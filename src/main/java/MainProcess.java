import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.quicktime.QuickTimeMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class MainProcess {

    /**
     * path为文件路径
     */
    private static String path = "C:\\Users\\CunXie\\Pictures\\iCloud";

    /**
     * 在main()函数中根据需求去调用不同方法
     * @param args
     */
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int countAll = 0;
        int countSuccess = 0;

        System.out.println("Please enter the photos path\n" +
                "请输入照片或视频的路径");
        if (sc.hasNextLine()) {
            String temp = sc.nextLine();
            if (!temp.equals("")) {
                path = temp;
            } else {
                path = path;
            }
        }

        System.out.println("Please choose a function\n" +
                "请选择使用的方法");
        System.out.println("1. updateAllTime(recommend) 将全部文件的修改时间更新为文件的拍摄时间（推荐）");
        System.out.println("2. updatePicTime 将照片的修改时间更新为照片的拍摄时间");
        System.out.println("3. updateVideoTime 将视频的修改时间更新为视频的拍摄时间");
        System.out.println("4. readAll 显示文件的所有信息");
        if (sc.hasNextLine()) {
            int input = sc.nextInt();
            File directory;
            switch (input) {
                case 1:
                    directory = new File(path);
                    for (File file : directory.listFiles()) {
                        countAll++;
                        int flag = updateAllTime(file);
                        if (flag == 1) {
                            countSuccess++;
                        }
                    }
                    break;
                case 2:
                    directory = new File(path);
                    for (File pic : directory.listFiles()) {
                        countAll++;
                        int flag = updatePicTime(pic);
                        if (flag == 1) {
                            countSuccess++;
                        }
                    }
                    break;
                case 3:
                    directory = new File(path);
                    for (File video : directory.listFiles()) {
                        countAll++;
                        int flag = updateVideoTime(video);
                        if (flag == 1) {
                            countSuccess++;
                        }
                    }
                    break;
                case 4:
                    directory = new File(path);
                    for (File file : directory.listFiles()) {
                        countAll++;
                        int flag = readAll(file);
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
     * 将全部文件的修改时间更新为文件的拍摄时间（推荐）
     * @param file
     * @return 更新成功则返回1，否则返回0
     */
    private static int updateAllTime(File file) {
        int flag = 0;
        try {
            //默认假设文件为照片
            //更新照片
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    if (tag.getTagType() == 0x9003) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                        Date originalDate = simpleDateFormat.parse(tag.getDescription());
                        long time = originalDate.getTime();
                        file.setLastModified(time);

                        //更新成功
                        flag = 1;
                    }
                }
            }
            //照片更新不成功，即文件可能为视频
            //更新视频
            if (flag == 0) {
                metadata = QuickTimeMetadataReader.readMetadata(file);
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
                            file.setLastModified(time);

                            //更新成功
                            flag = 1;
                        }
                    }
                }
            }
        } catch (Exception e) {
            try {
                //照片和视频都没有更新成功，即文件可能完全没有拍摄时间属性
                //将该文件移动到WithoutAllTime文件夹
                if (flag == 0) {
                    File moveDirectory = new File(file.getParent() + "\\WithoutAllTime");
                    if (!moveDirectory.exists()) {
                        moveDirectory.mkdirs();
                    }
                    File moveFile = new File(moveDirectory.getPath() + "\\" + file.getName());
                    Files.move(file.toPath(), moveFile.toPath());
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return flag;
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
                File moveDirectory = new File(pic.getParent() + "\\WithoutPicTime");
                if (!moveDirectory.exists()) {
                    moveDirectory.mkdirs();
                }
                File moveFile = new File(moveDirectory.getPath() + "\\" + pic.getName());
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
                    File moveDirectory = new File(video.getParent() + "\\WithoutVideoTime");
                    if (!moveDirectory.exists()) {
                        moveDirectory.mkdirs();
                    }
                    File moveFile = new File(moveDirectory.getPath() + "\\" + video.getName());
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
     * 显示文件的所有信息
     * @param file
     * @return 显示成功则返回1，否则返回0
     */
    private static int readAll(File file) {
        int flag = 0;
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
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
