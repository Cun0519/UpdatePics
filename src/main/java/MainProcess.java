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

public class MainProcess {

    /**
     * path为照片路径
     * movePath为移动文件夹路径
     * videoPath为视频路径
     */
    private static String path = "C:\\Users\\CunXie\\Pictures\\iCloud";
    private static String movePath = "C:\\Users\\CunXie\\Pictures\\move\\";
    private static String videoPath = "C:\\Users\\CunXie\\Pictures\\MOV";

    /**
     * 在main()函数中根据需求去调用不同方法
     * @param args
     */
    public static void main(String[] args) {
        File directory = new File(videoPath);
        int count = 0;
        int moveCount = 0;
        for (File pic : directory.listFiles()) {
            int flag = updateVideoTime(pic);
            switch (flag) {
                case 1:
                    count++;
                case 0:
                    moveCount++;
            }
        }
        System.out.println(count);
        //System.out.println(moveCount);
    }

    /**
     * 将照片的修改时间更新为照片的拍摄时间
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
                        flag = 1;

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                        Date originalDate = simpleDateFormat.parse(tag.getDescription());
                        long time = originalDate.getTime();
                        pic.setLastModified(time);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 将视频的修改时间更新为视频的拍摄时间
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
                        flag = 1;

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
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 将没有拍摄时间的照片移动至另一文件夹
     * @param pic
     */
    private static void movePicWithoutTime(File pic) {
        try {
            File moveFile = new File(movePath + pic.getName());
            Files.move(pic.toPath(), moveFile.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示照片的所有信息
     * @param pic
     */
    private static void readPicAll(File pic) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(pic);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
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
    }
}
