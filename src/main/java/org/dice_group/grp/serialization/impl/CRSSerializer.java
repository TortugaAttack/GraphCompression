package org.dice_group.grp.serialization.impl;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;

import org.dice_group.grp.serialization.GraphSerializer;


public class CRSSerializer <T> implements GraphSerializer{

	@SuppressWarnings("unchecked")
	public  <T extends Number> byte[] serializeNumberList(List<T> val, Class<?> numberClass) {
		if(val.size()==0) {
			return new byte[] {};
		}
		if(numberClass.equals(Byte.class)) {
			return serializeByteList((List<Byte>) val);
		}
		if(numberClass.equals(Short.class)) {
			return serializeShortList((List<Short>) val);
		}
		if(numberClass.equals(Integer.class)) {
			return serializeIntegerList((List<Integer>) val);
		}
		if(numberClass.equals(Long.class)) {
			return serializeLongList((List<Long>) val);
		}
		return null;
	}

	public  byte[] serializeLongList(List<Long> l) {
		byte[] ser = new byte[1+l.size()*Long.BYTES];
		ser[0]=3;
		for(int i=0;i<l.size()*Long.BYTES;) {
			byte[] curI = ByteBuffer.allocate(Long.BYTES).putLong(l.get(i/Long.BYTES)).array();
			for(byte b : curI) {
				ser[1+i++] = b;
			}
		}
		return ser;
	}	
	
	public  byte[] serializeIntegerList(List<Integer> l) {
		byte[] ser = new byte[1+l.size()*Integer.BYTES];
		ser[0]=2;
		for(int i=0;i<l.size()*Integer.BYTES;) {
			
			byte[] curI = ByteBuffer.allocate(Integer.BYTES).putInt(l.get(i/Integer.BYTES)).array();
			for(byte b : curI) {
				ser[1+i++] = b;
			}
		}
		return ser;
	}	
	
	public  byte[] serializeShortList(List<Short> l) {
		HashSet<Short> props = new HashSet<Short>();
		for(Short s : l){
			props.add(s);
		}

		byte[] ser = new byte[1+l.size()*Short.BYTES];
		ser[0]=1;
		for(int i=0;i<l.size()*Short.BYTES;) {
			byte[] curI = ByteBuffer.allocate(Short.BYTES).putShort(l.get(i/Short.BYTES)).array();
			for(byte b : curI) {
				ser[1+i++] = b;
			}
		}
		return ser;
	}	
	
	public  byte[] serializeByteList(List<Byte> l) {
		byte[] ser = new byte[1+l.size()*Byte.BYTES];
		ser[0]=0;
		for(int i=0;i<l.size()*Byte.BYTES;) {
			byte[] curI = ByteBuffer.allocate(Byte.BYTES).put(l.get(i)).array();
			for(byte b : curI) {
				ser[1+i++] = b;
			}
		}
		return ser;
	}
	
	@Override
	public <T extends Number> byte[] serialize(List<T> val, List<Integer> colRow, List<Integer> rowPtr) {
		//serialize lists individually
		byte[] serCol = serializeIntegerList(colRow);
		byte[] serRow = serializeIntegerList(rowPtr);
		byte[] serVal;
		if(val.size() >0) {
			serVal = serializeNumberList(val, val.get(0).getClass());
		}else {
			serVal = new byte[] {};
		}
		//serCol and serVal have to be same size -> only need one indicator 
		byte[] colSize = ByteBuffer.allocate(Integer.BYTES).putInt(serCol.length).array();
		// create concat of bytes to save
		byte[] ret = new byte[colSize.length+serCol.length+serVal.length+serRow.length];
		System.arraycopy(colSize, 0, ret, 0, colSize.length);
		System.arraycopy(serCol, 0, ret, colSize.length, serCol.length);
		System.arraycopy(serVal, 0, ret, colSize.length+serCol.length, serVal.length);
		System.arraycopy(serRow, 0, ret, colSize.length+serCol.length+serVal.length, serRow.length);
		return ret;
	}

	
	
}
