![logo](docs/wangling.webp)
# HFT - 刷机亡灵
`Heizi Flashing Tools``黑字刷机工具箱`又名 `刷级亡灵`
> 这套软件本来取名为hft黑字刷机套件，但感觉太拗口了，所以改了，叫刷机亡灵套件。所以不管叫hft也好叫刷机亡灵也好，都是同一个软件哈。

## What is HFT？ 刷级亡灵的具体定义
HFT主要围绕ADB和Fastboot做GUI处理，致力于可高度扩展、可下意识使用的刷机软件，以友好易用的方式连结小白和大佬。
> f**k the manual

## Why HFT? 为何非得是刷级亡灵?
刷机亡灵不是一步就成，而是对Fastboot使用的可能性探索缓慢前进的成果。
使用工具、总结使用规律、创造新的工具，这是第三个回合，你一定会认同它。
### 第一次操作的简化：Batch脚本
> * 项目地址 https://github.com/ElisaMin/LGG7-batch-Scripts  

如果你有幸使用过fastboot刷机，那么你一定会知道整个过程非常繁琐，首先你需要打开一个终端 确保这个终端可以找到Fastboot.exe，然后输入fastboot 和一些别的指令，偶尔刷一次是可以的，但是多次刷入非常难受。  
然后我学习了一点Batch脚本对整个过程进行了简化，打开 选择 拖入文件 刷入，扔掉了手打指令，互动式的刷机，非常棒的体验，即使后面我对这个工具进行了图形化进行推广，但还是不敌这个的流畅体验。  
![lazybox](docs/lazybox.jpg)
### 第二次简化：图形化工具站
> * 项目地址 https://github.com/ElisaMin/kotlin-swing-dsl-and-simple

![heizitools](docs/ht.jpg)   
我没有停下对刷机工具的探索，我写了一个图形化的工具，我花了一堆心思在上面思考fastboot的xxx功能应该怎么通过图形化展现出来。回过头来看这次的探索是阴沟里翻船了。这整个界面看起来比较混乱，属于对fastboot抽象成为图形界面的直白展示，并没有多人性化，而且文件选择器非常难用，每次都得疯狂的找，即使是作为开发者的我 也没有怎么使用这个软件。  
![fileChooser](docs/filechooser.jpg)  
就这个文件选择器，谁用谁知道的＊＊，如果你发现那个软件的文件选择器长这个样子那么一定是用Java开发的。
简单来说之前开发的软件都是一个庞大的工作站，你得打开这个工作站，才能选择里面的其中一个小功能使用。  
### 第三次简化：刷机套件
**我对Fastboot刷机工具的探索到这里进入了一个新的篇章。**  
![Image Flash Tool](docs/ift.png)  
所以这次我改变了逻辑，现在的刷机工具更像是一个文件编辑器，使用逻辑是你打开一个文件 处理这个文件，然后拔＊无情。基本上有过一定软件使用经验的人，都能做到下意识使用这款软件，因为这就像是你在打开某个图片音乐，然后浏览内容。     

## HFT套件里面都有啥
QPST用过吧？全称 `Qualcomm Product Support Tools` 安装之后就能用QFIL等等等等，开始菜单里面也会多出很多个软件，软件UI参差不齐，有比较现代的，也有上个世纪风格的。类似于Qpst，Hft在安装后也会安装几个子软件进去，可供完成一些刷机的操作。  

### FFT 
![FastbootFlashTool](docs/fft.png)  
Fastboot FlashTool，中文译名Fastboot管理器，有切换AB分区、分区管理等等操作，基本上满足了大部分的Fastboot刷机需求。
### IFT
![Image Flash Tool](docs/ift.png)  
Image FlashTool 镜像刷入工具，在电脑上面操作的话就是双击打开一个镜像，按照指示疯狂下一步就刷好机了，例如你要刷whyred_twrp_recovery.img这个红米的twrp，不需要打开什么软件、cmd，直接双击文件，别告诉我你看不懂这个是啥意思。

## 实战 
### LG V60三十秒刷TWRP 
https://www.bilibili.com/video/BV1LS4y1G7b9
### LG V60 解BL教程
https://bbs.lge.fun/thread-2551.htm

# modules
### Image Flash Tool
镜像刷入工具
### Khell
kotlin shell lib 
### logger
kotlin log lib
### compose.desktop.core
contains a fragment impl

# Videos
http://b23.tv/X6TEpfw

# 咕咕咕
INTP的日常项目搁置。
