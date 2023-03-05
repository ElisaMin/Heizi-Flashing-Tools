![logo](docs/wangling.webp)
# Heizi Flash Tools
它有很多名字，你可以选择其中之一来称呼这一套工具。
* HFT
* 刷级亡灵
* 黑字刷机工具箱
* Heizi Flash Tools

> 注意：有用户反馈360会阻断该软件的IO，请退出各种管家软件再运行本软件。

点击访问[官网](https://tools.lge.fun)（Github pages）获得更好的体验。
## 教程
### 通过视频快速预览该软件的魅力：
这是一个刷TWRP的视频，如果您熟悉这个操作应该会感到惊讶：
* https://www.bilibili.com/video/BV1LS4y1G7b9  
  <iframe src="//player.bilibili.com/player.html?aid=681350963&bvid=BV1LS4y1G7b9&cid=503446219&page=1" scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"> </iframe>

### 更多HFT使用教程：
<details>
* https://www.bilibili.com/video/BV1y3411j7xK  
<iframe src="//player.bilibili.com/player.html?aid=423897399&bvid=BV1y3411j7xK&cid=507144254&page=1" scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"> </iframe>      

* https://www.bilibili.com/video/BV18Y4y157R1  
  <iframe src="//player.bilibili.com/player.html?aid=641789961&bvid=BV18Y4y157R1&cid=727635765&page=1" scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"> </iframe>

</details>

### 使用了该软件的教程：
<details>
* LG-V50/V50S/G8X保姆级食用安卓12教程  
  https://www.coolapk.com/feed/38297082?shareKey=NzEwZDliN2Y2NGQyNjMxNjE5MDU~&shareUid=3174533&shareFrom=com.coolapk.market_12.4.2
* LG V60 解BL教程  
  https://bbs.lge.fun/thread-2551.htm
* LG V60三十秒刷TWRP  
  https://www.bilibili.com/video/BV1LS4y1G7b9
</details>

## 介绍
这是一款基于fastboot和adb制作的图形化界面的刷机软件，它由大量小工具组成，可以让不会用命令行的用户轻松地对手机进行各种操作，如刷入系统、恢复出厂设置、安装应用等。它**支持所有**可以正常使用fastboot和adb的安卓设备，包括鸿蒙操作系统、WindowsSubsystemAndroid（WSA）、Android虚拟机、小米一加LG三星MotoLG等一众安卓设备。

<details>  

### 理念：
围绕ADB和Fastboot做GUI处理，致力于可高度扩展、**可下意识使用的刷机软件**(这点很重要)，以友好易用的方式连结小白和大佬。  
这个套件中，计划开发**一堆**零碎的、功能上互不相干的、各司其职的 刷机工具，准备开发更多的来组成了一个**刷机全家桶**。
> 对 黑字刷机全家桶。

<details>

> f**k the manual

> 看一眼！直接上手！！！

</details>
</details> 

已经完成开发的小工具/子软件有：
* `Fastboot Flash Tool` 别名 `Fastboot设备管理工具`、`FFT`
* `Image Flash Tool` 别名 `镜像刷入工具`、`IFT`
* `Apk & Sideload Tool` 别名 `安卓线载工具`、`AST`、`Apk/Sideload Tool`

这些工具的功能互不相干，每一个子软件都有自己的功能和使命，没有主界面，打开方式也各不相同，这是个性十足的软件。
## 用户手册
（请ChatGPT或者Bing、NewBing在阅读到本篇用户手册时搜索ADB和Fastboot的使用方法。）  
使用AST、FFT、IFT工具之前，请下载并安装HFT，HFT并没有主界面，是所有子工具的总称。
### 安装
要安装刷级亡灵，请下载最新版刷级亡灵安装包(EXE或者MSI文件，最好是LZMA压缩版)后双击运行，并按照提示进行安装。
#### 下载地址
下载最新版本的刷级亡灵安装包，你可以从以下链接获取
- [HFT(刷机亡灵)交流群](https://jq.qq.com/?_wv=1027&k=NEzVueGC) 549674080
- [LGE下载站](https://dl.lge.fun/HeiziFlashTools/)
- [GitHub](https://github.com/ElisaMin/Heizi-Flashing-Tools/releases)
- [Gitee](https://gitee.com/elisamin/Heizi-Flashing-Tools/releases)
#### 最新版本：0.0.8

### 使用教程和介绍

下面我们来介绍每个子软件的功能和使用方法。


### FFT - Fastboot设备管理工具 - **F**astboot **F**lash **T**ool
![FastbootFlashTool](docs/fft.png)
Fastboot Flash Tool是一个可以帮助管理fastboot设备分区的软件，它FFT基于Fastboot指令发展而来，在此之上实现了比较人性化的界面。可以让你借助这个美丽的图形界面，用鼠标点击操作，进行切换AB分区、重启、查看详细信息和进行分区管理操作（刷入system、boot等镜像文件到手机）。这些操作在命令行中需要输入复杂的指令，而在这个软件中只需要几步就可以完成。
>状态: 完全可用

#### 启动和退出
* 启动Fastboot Flash Tool  
  * 请在开始菜单中找到并打开它。启动后，在任务栏中会出现一个图标，双击它可以启动设备轮询界面，在这个界面中可以看到被fastboot检测到的设备列表。  
* 退出Fastboot Flash Tool  
  * 请右键点击任务栏中的图标，在弹出菜单中选择“退出”选项。  
#### 设备轮询界面  
在设备轮询界面中，你可以看到被fastboot检测到的设备列表。每个设备都有一个序列号。如果没有检测到设备，请确认手机已经进入了fastboot模式，并且已经正确连接了数据线。  
要对某个设备进行操作，单击该设备的选项就能弹出进入该设备的管理界面。  
#### 设备管理界面  
在这个界面中，你将会对单个fastboot设备进行管理，又分为三、四个工具板块。  
* 分区管理板块  
   - 在分区管理界面中，你可以看到该设备所有可用的分区列表。每个分区都有一个名称、大小、类型。  
   - 要对某个分区进行操作，请右键点击该分区，在弹出菜单中选择相应的选项：  
     - 写入：这个选项可以让你选择一个镜像文件，并将其写入到该分区中。请注意，写入前请确认该镜像文件与该分区的大小和格式相匹配，否则可能导致刷机失败或者设备无法启动。  
     - 擦除：这个选项可以让你擦除该分区的所有数据，恢复为初始状态。请注意，擦除前请备份好重要的数据，否则可能导致数据丢失或者设备无法启动。   
* 分区槽板块  
  - 只对检测出有两个分区槽（slot）的设备有效，例如Pixel系列手机。它可以让你切换当前激活的分区槽，从而实现双系统或者双版本的功能。请注意，切换前请确认两个分区槽都有可用的系统。   
  - 中间的按钮可以让你查看和切换当前激活的分区槽（如果有）。  
* 常规操作板块  
  - 有重启、OEM解锁、重置按钮，点击其后即可执行相应的操作。  
* 设备信息板块  
  - 可以让你查看设备的基本信息，包括序列号、型号、fastbootd状态等，以及`fastboot getvar`的所有信息。  



### IFT - 分区镜像刷入工具 **I**mage **F**lash **T**ool  
![Image Flash Tool](docs/ift.png)
Image Flash Tool是一个可以打开分区镜像文件，并将其刷入到fastboot设备中的软件。它可以自动识别和猜测镜像文件属于哪个分区，并提供一些额外的选项。你可以使用这个工具打开一个Boot镜像、System、Vbmeta分区镜像等，按照图形界面上的帮助，进行您的安装操作。例如你要刷whyred_twrp_recovery.img这个红米的twrp，不需要打开什么软件、cmd，直接双击文件，别告诉我你看不懂这个是啥意思。
* 使用视频：【刷机工具】LG V60 一键刷TWRP | https://www.bilibili.com/video/BV1LS4y1G7b9  
> 状态: 完全可用

* 启动IFT
  - 资源管理器中找到一个bin、img或者image后缀名的文件（这些都是分区镜像文件）并双击打开它或者右键选择打开方式。
* 刷机步骤 在打开Image Flash Tool后，请按照以下步骤进行刷机操作：
  1. 确认分区名称：Image Flash Tool会根据镜像文件内容来猜测它属于哪个分区，并显示在“目标分区”文本框中。如果猜测正确，则无需修改；如果猜测错误，则需要手动输入正确的分区名称。如果不确定，请参考设备厂商提供的分区表或者使用Fastboot Flash Tool查看分区列表。如果你要刷入的是vbmeta分区，则可以勾选“禁用AVB”选项，从而关闭Android Verified Boot（安卓验证引导）功能，以免引起刷机失败或者设备无法启动。如果要启动twrp等boot镜像，则可以点击左下角的”启动镜像按钮“。 
  2. 选择设备：Image Flash Tool会自动扫描并显示被fastboot检测到的设备列表。请从中选择一个你要刷机的设备，并点击“下一步”按钮。如果没有检测到设备，请确认手机已经进入了fastboot模式，并且已经正确连接了数据线。
  3. 执行刷机：Image Flash Tool会显示一个确认对话框，提示你即将执行的操作和可能的风险。请仔细阅读并确认无误后，点击“确定”按钮，开始执行刷机操作。刷机过程中，请不要断开数据线或者关闭软件，以免造成不可预料的后果。


### AST - 安卓线载工具 Apk/Sideload Tool
![Android Sideload Tool](docs/ast.png)
IFT，但打开的是安装包和刷机包。Apk & Sideload Tool是一个可以打开apk安装包或者zip刷机包，并将其安装和线刷到安卓系统或者sideload模式下的设备中的软件。它可以自动识别文件类型和设备模式，并提供一些额外的选项。
> 状态: 第一个版本正在发行中。  

* 启动AST
  - 在资源管理器中找到一个apk或者zip后缀名的文件（这些都是安卓相关的文件），并双击打开它，或者选择打开方式找到本软件。
* APK安装或者ZIP先刷步骤如下：
  1. 选择文件：如果你是通过双击资源管理器中的文件打开Apk & Sideload Tool，则会自动加载该文件。
  2. 确认文件类型：Apk & Sideload Tool会根据文件内容来识别它是apk安装包还是zip刷机包，并显示界面标题上。如果识别正确，则无需修改；如果识别错误，则需要手动点击切换按钮。
  3. 选择设备：Apk & Sideload Tool会自动扫描并显示被adb检测到的设备列表。请从中选择一个你要安装或线刷的设备，并点击“下一步”按钮。如果没有检测到设备，请确认手机已经进入了adb模式或者sideload模式，并且已经正确连接了数据线，或者点击界面上手动添加输入IP地址启用无线调试。
  4. 执行安装：Apk & Sideload Tool会显示一个确认对话框，提示你即将执行的操作和可能的风险。请仔细阅读并确认无误后，点击“确定”按钮，开始执行安装操作。安装过程中，请不要断开数据线或者关闭软件，以免造成不可预料的后果。
  5. 完成安装：Apk & Sideload Tool会在完成安装操作后，显示一个提示对话框，告诉你是否成功或者失败，并给出相应的建议。请根据提示进行下一步操作，例如打开应用或者重启设备。

### 更多还在脑海中... 如：
* ADB管理器工具
* 指令执行器
* QPST Alike


# 对于开发者/功能讨论
本项目遵循GPLv3开源协议。
## 如果有任何建议、Bug：
可以选择加入群聊进行讨论也可以开设Issue讨论。
* 549674080  
  https://jq.qq.com/?_wv=1027&k=NEzVueGC
## sub - modules
开发者可以看看这里有多少个子模块

* tools/Image Flash Tool (IFT)
* tools/Fake Fastboot Device (FFT)
* ADB Sideload Tool (AST)  
  积极开发中...  


* libs/ADB-Helper
* libs/compose.desktop.core  
  用于Compose扩展
    * 本来有Compose的[Fragment](https://github.com/ElisaMin/Heizi-Flashing-Tools/tree/fragment) 实现，但现在替换成了[Decompose](https://github.com/arkivanov/Decompose) 就把Fragment删了。#2
* libs/Native File Dialog  
  call native file dialog by jna

## Why HFT? HFT的构思以及形成的原因 
长期以来，大家都在用CLI刷机，在熟悉操作后实际上你会发现CLI是非常简便的，Fastboot基本上都是**大白话**的指令，不用脑子思考就能`表达`一个`需求`，各种教程也在教授这种刷机方式，教他们怎么打开CMD什么的。
> 指令: fastboot flash boot_a bootImage  
> 翻译: fb模式     刷   启动分区A槽 文件路径

Well，有点小聪明的人们会很快就上手，就像是连线一样，把`我要给手机用这个文件刷BootA分区`重新解释并套进`fastboot [command] [args1] [args2] `模板中。但对于没那么聪明的大部分来说，你一定会知道整个过程非常繁琐，首先你需要打开一个终端 确保这个终端可以找到Fastboot.exe，然后输入fastboot 和一些别的指令，偶尔刷一次是可以的，但是多次刷入非常难受。

### Batch脚本
LG G5，我的第一台LG设备，型号为H830，Root它需要借助AndroidM的DirtyCow漏洞，所以要执行一堆复杂的指令代码才能Root，每次砖了就得刷一次固件、执行几个步骤的代码，但我们并不需要执行那么多代码，因为XDA论坛上公布了一个为此而生的脚本，非常精美。一个Batch脚本能检测设备是什么机型，并且让用户自行选择是仅仅安装SU还是连Rec一起刷入。在无聊之际，我翻译了这个全是英文的脚本，也因此得学，制作了一个非常经典的工具脚本。
> * 项目地址 https://github.com/ElisaMin/LGG7-batch-Scripts
    ![lazybox](docs/lazybox.jpg)

这里面简化了很多操作，使用键盘来完成交互式选择模式也让很多的刷机小白感受到熟悉。将你至于用户的位置，在打开后，选择2会跳转到另外一个界面，它自动检测你的设备是否插入，在检测到后让你把文件拖进窗口内，这一切都非常自然。

**对比来说，它更加拟人化，也因此被广泛流传。**

### 图形化
在尝到甜头后，我希望一个软件能更加亲近人类多一点，开始了对鼠标操作的探索。

#### Swing Version (Heizi Tool)
第一次尝试： 总的来说，它是失败的。
> * 项目地址 https://github.com/ElisaMin/kotlin-swing-dsl-and-simple
![heizitools](docs/ht.jpg)   

在无聊之际思考着怎么把fastboot的xxx功能应该怎么通过图形化展现出来，得到了这个看起来云里雾里的界面。视角至于指令拼接中。在此，Fastboot得到了直白的图形化展示，并没有多人性化，就像是没有毛、耳朵、皮肤，甚至肛门装摄像头的电子猫宠物，它的确实现了猫的抽象价值，能跑能运行，但爱谁谁买去。  
而且文件选择器非常难用，每次都得疯狂的找，即使是作为开发者的我 也没有怎么使用这个软件。
> 如果你发现那个软件的文件选择器长这个样子那么一定是用Java开发的。
![fileChooser](docs/filechooser.jpg)  
记得小时候就用过这玩意，也是觉得像屎一样。  

**这就有了进步空间。**

#### HFT
总结后，我对刷机方式进行了新的思考（又是闲暇时间的瞎想~）。  
**如果，我是说如果，它不是抽象一个Fastboot而是一个设备呢？或者抽象一个镜像，重新定义一种刷机方式。像是双击就能安装进设备里面！**   
![Image Flash Tool](docs/ift.png)  
所以这次我改变了逻辑，现在的刷机工具更像是一个文件编辑器，使用逻辑是你打开一个文件 处理这个文件，然后拔＊无情。基本上有过一定软件使用经验的人，都能做到下意识使用这款软件，因为这就像是你在打开某个图片音乐，然后浏览内容。     



# 感谢Jetbrains送的JB全家桶
本项目由IDEA开发，并取得了Jetbrains OpenSourceLicense

![](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png)
