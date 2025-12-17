import java.io.*;

public class ResumeFileTransfer {

    private static final int CHUNK_SIZE = 8 * 1024 * 1024; // 8 MB chunks


    public static void main(String[] args) {

        FileChooserUtil.chooseFile();
        String sourcePath= FileChooserUtil.selectedPath;
        String sourceFileName=FileChooserUtil.selectedFileName;

        if (sourcePath != null) {
            System.out.println("Selected file path: "+sourcePath);
            System.out.println("Selected file name: "+sourceFileName);
        } else {
            System.out.println("No file selected.");
            return;
        }


        //User Select Destination Path
        FileChooserUtil.chooseFolderAndGetPath();
        String destPath = FileChooserUtil.SelectedDestinationFolder;

        if (destPath != null) {
            System.out.println("Selected folder path:");
            System.out.println(destPath);
        } else {
            System.out.println("No folder selected.");
            return;
        }



        if (args.length >= 2) {
            sourcePath = args[0];
            destPath   = args[1];
        } else {
            System.out.println("Usage: java ResumeFileTransfer <sourceFile> <destFile>");
            System.out.println("Example 1 (Laptop → USB): java ResumeFileTransfer D:/Entertainment/E01.mkv H:/E01.mkv");
            System.out.println("Example 2 (USB → Laptop): java ResumeFileTransfer H:/E01.mkv D:/Entertainment/E01.mkv");
            System.out.println("Using default paths for testing...");
        }

        File sourceFile = new File(sourcePath);
        File destFile   = new File(destPath);
        File logFile    = new File(destFile.getName() + ".transfer.log"); // unique log per file

        try {
            long resumePosition = loadProgress(logFile);
            copyFileWithResume(sourceFile, destFile, resumePosition, logFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }///////////////

    private static void copyFileWithResume(File source, File dest, long resumePosition, File logFile) throws IOException {
        if (!source.exists()) {
            System.out.println("Source file not found: " + source.getAbsolutePath());
            return;
        }

        long fileSize = source.length();
        System.out.println("File size: " + fileSize / (1024 * 1024) + " MB");

        try (RandomAccessFile src = new RandomAccessFile(source, "r");
             RandomAccessFile dst = new RandomAccessFile(dest, "rw")) {

            src.seek(resumePosition);
            dst.seek(resumePosition);

            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            long totalCopied = resumePosition;

            while ((bytesRead = src.read(buffer)) != -1) {
                dst.write(buffer, 0, bytesRead);
                totalCopied += bytesRead;

                // Save progress
                saveProgress(totalCopied, logFile);

                double percent = (totalCopied * 100.0) / fileSize;
                System.out.printf("Progress: %.2f%%%n", percent);
            }

            System.out.println("Transfer complete!");
            clearProgress(logFile);
        }
    }///////////////////////////////////

    private static void saveProgress(long position, File logFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
            writer.write(Long.toString(position));
        }
    }///////////////////////////////////

    private static long loadProgress(File logFile) throws IOException {
        if (logFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line = reader.readLine();
                return Long.parseLong(line.trim());
            }
        }
        return 0;
    }///////////////////////////////////

    private static void clearProgress(File logFile) {
        if (logFile.exists()) {
            logFile.delete();
        }
    }///////////////////////////////////
}
