
该方式在5.0或者国产某些系统上面无效，所以默认不开启，需要可根据以下方式配置。

第一步：
在主程序模块的gradle根节点加入
repositories {
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
}

第二步：
还是主程序模块添加如下依赖
    compile 'com.github.droidwolf:libfileobserver:0.0.1-SNAPSHOT'

第三步：
创建指定对象（源码为当前目录下的JS文件）
ProcessWatcher  守护进程
UninstallWatcher 卸载后打开指定网站
WatchDog 初始化入口

第四步
调用
     Subprocess.create(this, WatchDog.class);

第五步
在创建的指定对象里的doSomething实现自己的逻辑（重启或者打开指定网站）