package sviolet.lib.io.copy;

import java.io.File;
import java.io.IOException;

public interface FileCopyer {
	public void copy(File source,File target) throws IOException;
}
