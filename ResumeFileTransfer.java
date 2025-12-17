import java.io.*;

public class ResumeFileTransfer {

    private static final int CHUNK_SIZE = 8 * 1024 * 1024; // 8 MB chunks

    public static void main(String[] args) {

        String sourcePath = null;
        String destPath = null;

        File file = new File("Save Path.txt");

        if (file.exists()) { //yes save path file exist

            System.err.println("File Exist");

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {

                sourcePath = br.readLine();
                destPath = br.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else { //No file exist

            System.out.println("File does NOT exist");

            // User select Source File for transfer
            FileChooserUtil.chooseFile();
            sourcePath = FileChooserUtil.selectedPath;
            String sourceFileName = FileChooserUtil.selectedFileName;

            if (sourcePath != null) {
                System.out.println("Selected file path: " + sourcePath);
                System.out.println("Selected file name: " + sourceFileName);
            } else {
                System.out.println("No file selected.");
                return;
            }

            // User Select Destination Path
            FileChooserUtil.chooseFolderAndGetPath();
            destPath = FileChooserUtil.SelectedDestinationFolder;

            if (destPath != null) {
                System.out.println("Selected folder path:");
                System.out.println(destPath);
            } else {
                System.out.println("No folder selected.");
                return;
            }

            try {
                if (file.createNewFile()) {
                    System.out.println("Save Path.txt created successfully");
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(sourcePath);
                writer.newLine();
                writer.write(destPath);
                writer.close();

                System.out.println("Data written successfully!");

            } catch (Exception e) {

            }

        }

        if (args.length >= 2) {
            sourcePath = args[0];
            destPath = args[1];
        } else {
            System.out.println("Usage: java ResumeFileTransfer <sourceFile> <destFile>");
            System.out.println("Example 1 (Laptop → USB): java ResumeFileTransfer D:/Entertainment/E01.mkv H:/E01.mkv");
            System.out.println("Example 2 (USB → Laptop): java ResumeFileTransfer H:/E01.mkv D:/Entertainment/E01.mkv");
            System.out.println("Using default paths for testing...");
        }

        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);
        File logFile = new File(destFile.getName() + ".transfer.log"); // unique log per file

        try {
            long resumePosition = loadProgress(logFile);
            copyFileWithResume(sourceFile, destFile, resumePosition, logFile, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }///////////////

    private static void copyFileWithResume(File source, File dest, long resumePosition, File logFile, File file)
            throws IOException {
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
            clearProgress(logFile, file);
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

    private static void clearProgress(File logFile, File file) {
        if (logFile.exists()) {
            logFile.delete();
            file.delete();
        }
    }///////////////////////////////////
}
