# Music-artist-recommendation-system    
项目描述:     
搭建自己的推荐系统，首先考虑数据来源，以一个简单的音乐网站作为收集数据的工具。    
http://github.com/liwenliang0/Music-artist-recommendation-system/raw/master/image/web.png
搜集用户行为信息，某用户点击播放了哪位艺术家的歌曲，及其次数       
利用Spark  ALS 算法，根据用户播放过很多相同的歌曲来判断他们喜欢同一个艺术家的歌曲，最终给用户推荐适合他的艺术家的作品。     
文件描述:    
Artist_alias   第一列是有可能的错误艺术家ID，第二列是正确的艺术家ID，用做数据处理   
http://github.com/liwenliang0/Music-artist-recommendation-system/raw/master/image/artist_alias.png  
artist_data.txt.gz第一列艺术家ID，第二列是艺术家名称    
http://github.com/liwenliang0/Music-artist-recommendation-system/raw/master/image/artist_data.txt.gz.png  
 user_artist_data.txt   第一列用户ID,第二列艺术家ID，第三列点击播放次数       
http://github.com/liwenliang0/Music-artist-recommendation-system/raw/master/image/user_artist_data.txt..png    
项目实现流程    
http://github.com/liwenliang0/Music-artist-recommendation-system/raw/master/image/process.jpg    
实现步骤       
1.假设 user_artist_data.txt， artist_data.txt， artist_alias.txt 三个数据集为历史数据     
2.过滤掉数据集artist_data.txt中少量非法的行，有些没有制表符，有些不小心加入了换行符。      
3.将不正确的艺术家ID映射为正确的，例如将ID 为6803336 的艺术家映射为1000010的艺术家，通过广播变量，将artist_alias.txt 读入内存转为map 广播出去   
4.将训练集存储在HDFS 上的指定位置，格式parquet    
5.通过定时任务，将增量数据合并到训练集中     
http://github.com/liwenliang0/Music-artist-recommendation-system/raw/master/image/achieve.png     
6.定期将模型重新构建，并保存在HDFS上     
定时任务采用Quartz 定时任务调度框架     
http://github.com/liwenliang0/Music-artist-recommendation-system/raw/master/image/quartz.png   
7*.通过超参数的方式找到最优解     
8*.通过交叉验证方式获取最优的模型      
9. 给定用户能够为其推荐相应艺术家      













