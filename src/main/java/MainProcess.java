import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.quicktime.QuickTimeMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataReader;
import com.drew.metadata.Tag;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class MainProcess {

    /**
     * path为照片路径
     * videoPath为视频路径
     * PicWithoutTime为照片移动路径
     * videoWithoutTimePath为视频移动路径
     */
    private static String path = "C:\\Users\\CunXie\\Pictures\\iCloud";
    private static String videoPath = "C:\\Users\\CunXie\\Pictures\\MOV";
    private static String picWithoutTimePath = "C:\\Users\\CunXie\\Pictures\\PicWithoutTime\\";
    private static String videoWithoutTimePath = "C:\\Users\\CunXie\\Pictures\\VideoWithoutTime\\";

    /**
     * 在main()函数中根据需求去调用不同方法
     * @param args
     */
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int countAll = 0;
        int countSuccess = 0;

        System.out.println("1. updatePicTime");
        System.out.println("2. updateVideoTime");
        System.out.println("3. readPicAll");
        System.out.println("Please choose a function:");
        if (sc.hasNext()) {
            int input = sc.nextInt();
            File directory;
            switch (input) {
                case 1:
                    directory = new File(path);
                    for (File pic : directory.listFiles()) {
                        countAll++;
                        int flag = updatePicTime(pic);
                        if (flag == 1) {
                            countSuccess++;
                        }
                    }
                    break;
                case 2:
                    directory = new File(videoPath);
                    for (File pic : directory.listFiles()) {
                        countAll++;
                        int flag = updateVideoTime(pic);
                        if (flag == 1) {
                            countSuccess++;
                        }
                    }
                    break;
                case 3:
                    directory = new File(path);
                    for (File pic : directory.listFiles()) {
                        countAll++;
                        int flag = readPicAll(pic);
                        if (flag == 1) {
                            countSuccess++;
                        }
                    }
                    break;
                default:
                    System.out.println("Illegal input!");
                    return;
            }
        }

        System.out.println("总文件数：" + countAll);
        System.out.println("成功处理：" + countSuccess);
    }

    /**
     * 将照片的修改时间更新为照片的拍摄时间
     * 将没有拍摄时间的照片移动至另一文件夹
     * @param pic
     * @return 照片如果包含拍摄时间则返回1，否则返回0
     */
    private static int updatePicTime(File pic) {
        int flag = 0;
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(pic);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    if (tag.getTagType() == 0x9003) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                        Date originalDate = simpleDateFormat.parse(tag.getDescription());
                        long time = originalDate.getTime();
                        pic.setLastModified(time);

                        flag = 1;
                    }
                }
            }

            if (flag == 0) {
                File moveDirectory = new File(picWithoutTimePath);
                if (!moveDirectory.exists()) {
                    moveDirectory.mkdirs();
                }
                File moveFile = new File(picWithoutTimePath + pic.getName());
                Files.move(pic.toPath(), moveFile.toPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 将视频的修改时间更新为视频的拍摄时间
     * 将没有拍摄时间的视频移动至另一文件夹
     * @param video
     * @return 视频如果包含拍摄时间则返回1，否则返回0
     */
    private static int updateVideoTime(File video) {
        int flag = 0;
        try {
            Metadata metadata = QuickTimeMetadataReader.readMetadata(video);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    if (tag.getTagType() == 256) {
                        //日期转换
                        String stringTemp = tag.getDescription();
                        String[] stringArrayTemp = stringTemp.split(" ");
                        stringArrayTemp[1] = stringArrayTemp[1].substring(0, stringArrayTemp[1].length() - 1);
                        StringBuffer stringBufferTemp = new StringBuffer();
                        stringBufferTemp.append(stringArrayTemp[5] + ":");
                        stringBufferTemp.append(stringArrayTemp[1] + ":");
                        stringBufferTemp.append(stringArrayTemp[2] + " ");
                        stringBufferTemp.append(stringArrayTemp[3]);
                        //System.out.println(tag.getDescription());
                        //System.out.println(stringBufferTemp);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                        Date originalDate = simpleDateFormat.parse(stringBufferTemp.toString());
                        long time = originalDate.getTime();
                        video.setLastModified(time);

                        flag = 1;
                    }
                }
            }
        } catch (Exception e) {
            try {
                if (flag == 0) {
                    File moveDirectory = new File(videoWithoutTimePath);
                    if (!moveDirectory.exists()) {
                        moveDirectory.mkdirs();
                    }
                    File moveFile = new File(videoWithoutTimePath + video.getName());
                    Files.move(video.toPath(), moveFile.toPath());
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 显示照片的所有信息
     * @param pic
     */
    private static int readPicAll(File pic) {
        int flag = 0;
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(pic);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    flag = 1;
                    System.out.format("[%s] - %s = %s",
                            directory.getName(), tag.getTagName(), tag.getDescription());
                    System.out.println("");
                }
                if (directory.hasErrors()) {
                    for (String error : directory.getErrors()) {
                        System.err.format("ERROR: %s", error);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
}
