package sviolet.turquoise.model.math;

/**
 * Float平均值<p>
 * 
 * 记录最近sampleSize次数据样本，并输出平均值
 * 
 * @author S.Violet
 *
 */

public class AverageFloat {
	
	private float[] sample;//样本数据
	private int offset;//当前写入位置
	private boolean full;//样本容器是否填满
	
	/**
	 * 
	 * 无填充，起始样本数小于容器大小
	 * 
	 * @param sampleSize 样本容器大小
	 */
	public AverageFloat(int sampleSize){
		sample = new float[sampleSize];
		offset = 0;
		full = false;
	}
	
	/**
	 * 
	 * 用padding值填充满样本容器
	 * 
	 * @param sampleSize 样本容器大小
	 * @param padding 样本容器填充数据
	 */
	public AverageFloat(int sampleSize, float padding){
		this(sampleSize);
		
		for(int i = 0 ; i < sample.length ; i++){
			sample[i] = padding;
		}
		full = true;
	}
	
	/**
	 * 采样（记录一条数据）
	 * 
	 * @param data
	 * @return 返回当前平均值
	 */
	public float sampling(float data){
		sample[offset] = data;
		offset++;
		if(offset >= sample.length){
			full = true;
			offset = 0;
		}
		
		return getAverage();
	}
	
	/**
	 * 返回当前平均值
	 * 
	 * @return
	 */
	public float getAverage(){
		if(full){
			float sum = 0;
			for(int i = 0 ; i < sample.length ; i++){
				sum += sample[i];
			}
			return sum / sample.length;
		}else{
			float sum = 0;
			for(int i = 0 ; i < offset ; i++){
				sum += sample[i];
			}
			return sum / offset;
		}
	}
}
