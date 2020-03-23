# BlockPlay

    Nukkit Plugin BlockPlay——方块华容道
    插件使用GPL-3.0开源协议,详情百度,转载需授权

<> 功能介绍

    一款游戏插件 可无限设置房间
    华容道应该都知道是啥了吧(如果不知道的话详见百度百科)。这里的数字就用方块来代替了。所以需要建设一个模板供玩家参考。如下图

<>指令说明

    /bplay add [房间名] ------ 创建新房间
    /bplay set [房间名] ------ 设置房间
    /bplay del [房间名] ------ 删除房间
    
<>创建一个游戏的步骤

    首先输入/bplay add xxx
    之后再输入/bplay set xxx
    然后按照提示破坏方块即可

<>游戏区域设置要求

    -边框自行设置且不能使用羊毛自行建造模板
    -请使用竖立平面且最左上角必须设为点1，最右下角必须设为点2
    -破坏方块设置点1，然后破坏一个3x3大小的竖直平面的方块设置点2

<>配置文件说明

    state: 'true'
    room_world: basketball
    start_time: '5'
    pos1: -521+7+-916
    pos2: -521+5+-914
    direction: z+
    area: 9
    length: 3
    width: 3
    button_pos: -522+5+-917
    配置文件除start_time外请不要擅自修改
    start_time的作用是开始游戏的时间



