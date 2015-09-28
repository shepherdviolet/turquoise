package sviolet.lib.io.copy;

public class FileCopyerFactory {
	public final static int FAST_FILECOPYER = 0;
	public final static int BUFFERED_FILECOPYER = 1;
	
	public static FileCopyer getFileCopyer(int copyerType) throws Exception{
		if(copyerType == FAST_FILECOPYER)
			return new FastFileCopyer();
		else if(copyerType == BUFFERED_FILECOPYER)
			return new BufferedFileCopyer();
		
		throw new Exception("No Such FileCopyer");
	}
}
