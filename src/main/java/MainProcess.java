import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataReader;
import com.drew.metadata.Tag;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainProcess {

    private static String path = "C:\\Users\\CunXie\\Pictures\\iCloud";
    private static String movePath = "C:\\Users\\CunXie\\Pictures\\move\\";
    private static String videoPath = "C:\\Users\\CunXie\\Pictures\\MOV";

    public static void main(String[] args) {
        File directory = new File(path);
        int count = 0;
        int moveCount = 0;
        for (File pic : directory.listFiles()) {

        }
        System.out.println(count);
        System.out.println(moveCount);
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
