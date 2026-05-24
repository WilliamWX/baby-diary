package com.babydiary.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class VideoTranscodeService {

    private static final String FFMPEG_PATH = "C:/Users/24526/AppData/Roaming/npm/node_modules/ffmpeg-static/ffmpeg.exe";

    /**
     * Check if the video is HEVC/H.265 encoded by reading the MP4 stsd atom.
     * Returns true if the file should be transcoded to H.264.
     */
    public boolean isHevc(byte[] header) {
        // Look for "hvc1" or "hev1" in the first few KB (stsd box)
        for (int i = 0; i < header.length - 4; i++) {
            if (header[i] == 'h' && header[i+1] == 'v' && header[i+2] == 'c' && header[i+3] == '1') return true;
            if (header[i] == 'h' && header[i+1] == 'e' && header[i+2] == 'v' && header[i+3] == '1') return true;
        }
        return false;
    }

    /**
     * Transcode HEVC video to H.264. Returns the transcoded file, or the original if transcoding fails.
     */
    public File transcodeIfNeeded(MultipartFile file) throws IOException {
        Path tmpDir = Files.createTempDirectory("video-transcode-");
        File inputFile = new File(tmpDir.toFile(), "input" + getExtension(file.getOriginalFilename()));
        file.transferTo(inputFile);

        // Read header to detect codec
        byte[] header = new byte[8192];
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            fis.read(header);
        }

        if (!isHevc(header)) {
            // Already H.264 or other supported codec, return as-is
            return inputFile;
        }

        log.info("Detected HEVC video, transcoding to H.264: {}", file.getOriginalFilename());
        File outputFile = new File(tmpDir.toFile(), "output.mp4");

        ProcessBuilder pb = new ProcessBuilder(
                FFMPEG_PATH,
                "-y",
                "-i", inputFile.getAbsolutePath(),
                "-c:v", "libx264",
                "-preset", "fast",
                "-crf", "23",
                "-c:a", "aac",
                "-movflags", "+faststart",
                outputFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();
            // Must consume output stream to prevent process hang (buffer full)
            Thread drainer = new Thread(() -> {
                try (var is = p.getInputStream()) {
                    is.transferTo(OutputStream.nullOutputStream());
                } catch (IOException ignored) { }
            });
            drainer.start();
            int exit = p.waitFor();
            drainer.join();
            if (exit == 0 && outputFile.exists() && outputFile.length() > 0) {
                log.info("Transcode successful: {} -> {} bytes", inputFile.length(), outputFile.length());
                return outputFile;
            }
            log.warn("FFmpeg exited with code {}, falling back to original file", exit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Transcode interrupted, falling back to original file");
        }
        return inputFile;
    }

    /**
     * Extract the first frame from a video file as a JPEG cover image.
     */
    public File extractCover(File videoFile) throws IOException {
        Path tmpDir = Files.createTempDirectory("video-cover-");
        File coverFile = new File(tmpDir.toFile(), "cover.jpg");

        ProcessBuilder pb = new ProcessBuilder(
                FFMPEG_PATH,
                "-y",
                "-i", videoFile.getAbsolutePath(),
                "-vframes", "1",
                "-q:v", "2",
                coverFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();
            Thread drainer = new Thread(() -> {
                try (var is = p.getInputStream()) {
                    is.transferTo(OutputStream.nullOutputStream());
                } catch (IOException ignored) { }
            });
            drainer.start();
            int exit = p.waitFor();
            drainer.join();
            if (exit == 0 && coverFile.exists() && coverFile.length() > 0) {
                log.info("Cover extracted: {} bytes", coverFile.length());
                return coverFile;
            }
            log.warn("Cover extraction failed with exit code {}", exit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Cover extraction interrupted");
        }
        return null;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".mp4";
        return filename.substring(filename.lastIndexOf("."));
    }
}
