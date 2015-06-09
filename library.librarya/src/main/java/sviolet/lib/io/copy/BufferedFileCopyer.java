package sviolet.lib.io.copy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BufferedFileCopyer implements FileCopyer {
	/*
	 *较快的复制文件方法（可监控复制进度）<br>
	 * 目标文件的修改时间与原文件保持基本一致<br>
	 * 目标文件修改时间可能会偏大,偏差<2000ms, 例如:<br>
	 * 原始文件修改时间:****2356<br>
	 * 目标文件修改时间:****4000
	 * 
	 * @param source 源文件
	 * @param target 目标文件
	 */
    public void copy(File source, File target) throws IOException {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            while (in.read(buffer) != -1) {
                buffer.flip();
                out.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            throw e;
        } finally {
			try{
				in.close();
				out.close();
				inStream.close();
				outStream.flush();
				outStream.close();
			}catch(Exception e){
				
			}
        }
        target.setLastModified(source.lastModified());//使目标文件修改时间与源文件保持一致
    }
}
