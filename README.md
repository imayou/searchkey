# simple-searchkey
#### 同级目录文件搜索


### 运行
> java -jar -DFile.Path="/home/user1/interface_xml" search.war &

### 零时JDK环境脚本
```bash
export JAVA_HOME=/home/user1/tools/jdk1.8.0_131
$JAVA_HOME/bin/java -jar -DFile.Path="/home/user1/interface_xml" -DSearch.Threads="20" search.war &
```
> 备注：如果不指定报文路径File.Path，则前端需要传报文路径path，Search.Threads为搜索时线程池中的线程数，不指定的时候默认取cpu的核心数。
