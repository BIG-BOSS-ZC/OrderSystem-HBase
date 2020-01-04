package com.hopu.bigdata.hbaseapi;

import com.hopu.bigdata.model.Order;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
@Component
public class HBaseApi implements CommandLineRunner {
    private static Logger log = LoggerFactory.getLogger(HBaseApi.class);

    private static Admin admin;
    private static Table table;

    //连接集群
    public static Connection initHbase() throws IOException {

        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("hbase.zookeeper.quorum", "192.168.1.23,192.168.1.24,192.168.1.25");
        //集群配置↓
        //configuration.set("hbase.zookeeper.quorum", "101.236.39.141,101.236.46.114,101.236.46.113");
        configuration.set("hbase.master", "192.168.1.23:16010");
        Connection connection = ConnectionFactory.createConnection(configuration);
        return connection;
    }
    //创建表
    public static void createTable(String tableNmae, String[] cols) throws IOException {

        TableName tableName = TableName.valueOf(tableNmae);
        admin = initHbase().getAdmin();
        if (admin.tableExists(tableName)) {
            System.out.println("表已存在！");
        } else {
            HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
            for (String col : cols) {
                HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(col);
                hTableDescriptor.addFamily(hColumnDescriptor);
            }
            admin.createTable(hTableDescriptor);
        }
    }


    //插入数据
    //orderindex索引表
    public static void insertData(String tableName, BlockingQueue<Order> queueOrder) throws IOException{
        TableName tablename = TableName.valueOf(tableName);
        Table table = initHbase().getTable(tablename);
        //orderindex索引表
        TableName tablenameindex = TableName.valueOf("orderindex");
        Table indextable = initHbase().getTable(tablenameindex);
        while(true){
            try {
                Order order = queueOrder.poll(120, TimeUnit.SECONDS);
                String rowkey=order.getUserid()+"_"+order.getOrdertime();
                Put put = new Put((rowkey).getBytes());
                //参数：1.列族名  2.列名  3.值
                put.addColumn(Bytes.toBytes("info"),Bytes.toBytes("userid"),Bytes.toBytes(order.getUserid()+"")) ;
                put.addColumn(Bytes.toBytes("info"),Bytes.toBytes("orderid"), Bytes.toBytes(order.getOrderid()+"")) ;
                put.addColumn(Bytes.toBytes("info"),Bytes.toBytes("ordertime"), Bytes.toBytes(order.getOrdertime())) ;
                put.addColumn(Bytes.toBytes("info"),Bytes.toBytes("proids"), Bytes.toBytes(order.getProids().toString())) ;
                put.addColumn(Bytes.toBytes("info"),Bytes.toBytes("state"), Bytes.toBytes(order.getState())) ;


                Put putindex = new Put((order.getOrderid()+"").getBytes());
                //参数：1.列族名  2.列名  3.值
                putindex.addColumn(Bytes.toBytes("info"),Bytes.toBytes("rowkey"),Bytes.toBytes(rowkey)) ;

                table.put(put);
                indextable.put(putindex);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        table.close();
        indextable.close();
    }
    //获取原始数据
    public static void scanData(String tableName){
        try {
//            Table table= initHbase().getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            ResultScanner resutScanner = table.getScanner(scan);
            for(Result result: resutScanner){
                System.out.println("scan:  " + result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //根据rowKey进行查询
    public static Map<String,String> getDataByRowKey(String tableName, String rowKey) throws IOException {
        Map<String,String> orderdata=new HashMap<>();

        Table table = initHbase().getTable(TableName.valueOf(tableName));
        Get get = new Get(rowKey.getBytes());
        //先判断是否有此条数据
        if(!get.isCheckExistenceOnly()){
            Result result = table.get(get);
            for (Cell cell : result.rawCells()){
                String colName = Bytes.toString(cell.getQualifierArray(),cell.getQualifierOffset(),cell.getQualifierLength());
                String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                orderdata.put(colName,value);
                System.out.println(colName+" : "+value);
            }
        }

        return orderdata;
    }

    /**
     * 创建表
     * @param tableName    表名
     * @param columnFamily 列族名
     */
    public static boolean createTable(String tableName, List<String> columnFamily) {
        Admin admin = null;
        try {
            admin = initHbase().getAdmin();;
            List<ColumnFamilyDescriptor> familyDescriptors = new ArrayList<>(columnFamily.size());
            columnFamily.forEach(cf -> {
                familyDescriptors.add(ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(cf)).build());
            });
            TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(TableName.valueOf(tableName))
                    .setColumnFamilies(familyDescriptors)
                    .build();
            if (admin.tableExists(TableName.valueOf(tableName))) {
                log.debug("table Exists!");
            } else {
                admin.createTable(tableDescriptor);
                log.debug("create table Success!");
            }
        } catch (IOException e) {
            log.error(MessageFormat.format("创建表{0}失败", tableName), e);
            return false;
        } finally {
            close(admin, null, null);
        }
        return true;
    }

    /**
     * 预分区创建表
     *
     * @param tableName    表名
     * @param columnFamily 列族名的集合
     * @param splitKeys    预分期region
     * @return 是否创建成功
     */
    public static boolean createTableBySplitKeys(String tableName, List<String> columnFamily, byte[][] splitKeys) {
        Admin admin = null;
        try {
            if (StringUtils.isBlank(tableName) || columnFamily == null
                    || columnFamily.size() == 0) {
                log.error("===Parameters tableName|columnFamily should not be null,Please check!===");
                return false;
            }
            admin = initHbase().getAdmin();;
            if (admin.tableExists(TableName.valueOf(tableName))) {
                return true;
            } else {
                List<ColumnFamilyDescriptor> familyDescriptors = new ArrayList<>(columnFamily.size());
                columnFamily.forEach(cf -> {
                    familyDescriptors.add(ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(cf)).build());
                });
                TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(TableName.valueOf(tableName))
                        .setColumnFamilies(familyDescriptors)
                        .build();

                //指定splitkeys
                if (splitKeys == null)
                    splitKeys = getSplitKeys(null);
                admin.createTable(tableDescriptor, splitKeys);
                log.info("===Create Table " + tableName
                        + " Success!columnFamily:" + columnFamily.toString()
                        + "===");
            }
        } catch (IOException e) {
            log.error("", e);
            return false;
        } finally {
            close(admin, null, null);
        }

        return true;
    }

    /**
     * 自定义获取分区splitKeys
     */
    public static byte[][] getSplitKeys(String[] keys) {
        if (keys == null) {
            //默认为10个分区
            keys = new String[]{"0|", "1|", "2|", "3|", "4|",
                    "5|", "6|", "7|", "8|", "9|"};
        }
        byte[][] splitKeys = new byte[keys.length][];
        //升序排序
        TreeSet<byte[]> rows = new TreeSet<>(Bytes.BYTES_COMPARATOR);
        for (String key : keys) {
            rows.add(Bytes.toBytes(key));
        }

        Iterator<byte[]> rowKeyIter = rows.iterator();
        int i = 0;
        while (rowKeyIter.hasNext()) {
            byte[] tempRow = rowKeyIter.next();
            rowKeyIter.remove();
            splitKeys[i] = tempRow;
            i++;
        }
        return splitKeys;
    }

    /**
     * 根据startRowKey和stopRowKey遍历查询指定表中的所有数据
     *
     * @param tableName   表名
     * @param startRowKey 起始rowKey
     * @param stopRowKey  结束rowKey
     */
    public static Map<String, Map<String, String>> scanByStartEndRowkey(String tableName, String startRowKey, String stopRowKey) {
        Scan scan = new Scan();

        if (StringUtils.isNoneBlank(startRowKey) && StringUtils.isNoneBlank(stopRowKey)) {
            scan.withStartRow(Bytes.toBytes(startRowKey));
            scan.withStopRow(Bytes.toBytes(stopRowKey));
        }

        return queryData(tableName, scan);
    }


    /**
     * 通过表名以及过滤条件查询数据
     *
     * @param tableName 表名
     * @param scan      过滤条件
     */
    private static Map<String, Map<String, String>> queryData(String tableName, Scan scan) {
        //<rowKey,对应的行数据>
        Map<String, Map<String, String>> result = new HashMap<>();

        ResultScanner rs = null;
        // 获取表
//        Table table = null;
        try {
//            table = initHbase().getTable(TableName.valueOf(tableName));
            rs = table.getScanner(scan);
            for (Result r : rs) {
                //每一行数据
                Map<String, String> columnMap = new HashMap<>();
                String rowKey = null;
                for (Cell cell : r.listCells()) {
                    if (rowKey == null) {
                        rowKey = Bytes.toString(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                    }
                    columnMap.put(Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()), Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
                }

                if (rowKey != null) {
                    result.put(rowKey, columnMap);
                }
            }
        } catch (IOException e) {
            log.error(MessageFormat.format("遍历查询指定表中的所有数据失败,tableName:{0}"
                    , tableName), e);
        } finally {
            close(null, rs, table);
        }

        return result;
    }


    /**
     * 关闭流
     */
    private static void close(Admin admin, ResultScanner rs, Table table) {
        if (admin != null) {
            try {
                admin.close();
            } catch (IOException e) {
                log.error("关闭Admin失败", e);
            }
        }

        if (rs != null) {
            rs.close();
        }

        if (table != null) {
            try {
                table.close();
            } catch (IOException e) {
                log.error("关闭Table失败", e);
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        table=initHbase().getTable(TableName.valueOf("order1"));
    }
}
