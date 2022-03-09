# ImageFlashTool
镜像刷入工具
# 记录此刻的重构
## [17 21:55](https://github.com/ElisaMin/Heizi-Flashing-Tools/tree/aacb005a23d3e9ba3199f090ea7949d0b4bcc258)
刚刚发现了一个糊掉的东西，我把整个软件的单位抽象为了状态，因为状态A所以界面是A然后这个View长这样子，这个功能长这样子，这个用法，我把在重构前的Commit放在了标题上面。
你们可以去看一下，然后我花了一点时间去重新理解了一下如何用ViewModel进行重构。
#### 为什么要重构？
比如说整个软件有启动状态、运行状态、结束语状态。在启动状态时界面里面有一些选项可以给用户选择一些preconfig什么的类似的东西，然后再运行。
```json5
[{
  stateName: "launcher",
  view: ["checkBox","inputBox","bootBtn"],
  viewmodole: ["checkBoxBool","inputBoxStr"],
  functions: ["on checkbox click","on checkbox checked","on input","on boot click"]
},{
  stateName: "running",
  view: ["runningStateChecker",],
  viewmodole: ["stateChecker"],
  functions: [""]
}]
```
然后`运行状态`的子状态一部分来自`启动状态`，就像sql的外键一样，强耦合在了一起。无法更新和理解内部代码，正确地重构应该是:
#### 以界面作为单位
```json5
[{
  screenName:"launcher",
  view: ....,
  model: ....,
  viewModel: .....,
}]
```
## 18 11:42
这次的重构有点无从下手，太乱了。
## 2022 03 09 16：34
你太慢了哈哈哈 我用decompose帮你把它重构完了 估计你不太喜欢这次的重构。