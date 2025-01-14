package com.howbuy.hadoop.mr.join;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class JoiningMapper extends Mapper<LongWritable, Text, TaggedKey, Text> {

    private int keyIndex;
    private Splitter splitter;
    private Joiner joiner;
    private TaggedKey taggedKey = new TaggedKey();
    private Text data = new Text();
    private int joinOrder;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        keyIndex = Integer.parseInt(context.getConfiguration().get("keyIndex"));
        String separator = context.getConfiguration().get("separator");
        splitter = Splitter.on(separator).trimResults();
        joiner = Joiner.on(separator);
        FileSplit fileSplit = (FileSplit)context.getInputSplit();
        joinOrder = Integer.parseInt(context.getConfiguration().get(fileSplit.getPath().getName()));
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    	if(value.toString().trim().length() == 0){
    		context.getCounter("join","null_val").increment(1);
    		return;
    	}
        List<String> values = Lists.newArrayList(splitter.split(value.toString()));
        String joinKey = values.remove(keyIndex);
        String valuesWithOutKey = joiner.join(values);
        taggedKey.set(joinKey, joinOrder);
        data.set(valuesWithOutKey);
        context.write(taggedKey, data);
    }

}
