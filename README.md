# CurveView
轻量级、可高度定制化的折线图

## 效果演示

|显示全部|滚动支持|
|---|----|
|![显示全部](https://raw.githubusercontent.com/auv1107/CurveView/master/art/curve_show_all.png)|![滚动支持](https://raw.githubusercontent.com/auv1107/CurveView/master/art/curve_scroll.gif)|

## 特点

- 支持样式定制
- 使用 adapter 方式集成数据，用法简单，极易理解
- 支持点上 8 个方向同时添加文字
- 支持显示全部长度或手动拖动

## 用法

### 1. 导入依赖

在项目`build.gradle`中添加:

```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
添加依赖
```gradle
dependencies {
        compile 'com.github.auv1107:CurveView:a0e576c041'
}
```

### 2. 添加 `CurveView`
```xml
<com.sctdroid.app.uikit.CurveView
    android:id="@+id/curve_view"
    android:layout_width="match_parent"
    android:layout_height="150dp"
    app:backgroundColor="#16b7df"
    app:corner="1px"
    app:contentPaddingStart="40dp"
    app:contentPaddingEnd="40dp"
    app:contentPaddingBottom="20dp"
    app:contentPaddingTop="20dp"
    app:strokeWidth="1px"
    app:showXLine="false"
    app:showXText="true"
    app:contentColor="@android:color/white"
    app:dotTextColor="@android:color/white"
    app:dotTextSize="8sp"
    app:axisTextSize="9sp"
    app:axisTextColor="@android:color/white"
    app:dotTextGravity="center_horizontal|bottom"
    app:showAll="true"
    app:unitWidth="100dp"
    />
```

**属性说明**

| 属性 | 类型 | 说明 |
|---|---|---|
| backgroundColor | color | 背景色，暂只支持颜色背景 |
| corner | dimension | 折线平滑度，0 为尖锐的折线，越大越平滑 |
| contentPaddingStart | dimension | 左(开始点)内容边距 |
| contentPaddingEnd | dimension | 右(结束点)内容边距 |
| contentPaddingBottom | dimension | 下内容边距 |
| contentPaddingTop | dimension | 上内容边距 |
| strokeWidth | dimension | 折线宽度 |
| showXLine | boolean | 是否绘制 x 轴 |
| showXText | String | 是否显示 x 轴文字 |
| contentColor | color | 折线和坐标轴颜色 |
| dotTextColor | color | 点标记文字颜色 |
| dotTextSize | dimension | 点标记文字尺寸 |
| axisTextSize | dimension | 坐标轴文字尺寸 |
| axisTextColor | color | 坐标轴文字颜色 |
| showAll | boolean | 是否显示所有点。`true`，显示所有点。`false`，每格宽度由 unitWidth 指定，支持手指拖动 |
| unitWidth | dimension | x 轴上相邻两点宽度，仅在 `showAll` 为 `false` 时有效 |


### 3. 添加 `Adapter`

```java
CurveView curveView = (CurveView) findViewById(R.id.curve_view);
curveView.setAdapter(new CurveView.Adapter() {

    String text = "吾生也有涯，而知也无涯";

    /**
     * @return 点的数量
     */
    @Override
    public int getCount() {
        return 7;
    }

    /**
     * level 是 y 轴高度，在 minLevel 和 maxLevel 之间
     * @param position
     * @return 返回当前 position 的 level
     */
    @Override
    public int getLevel(int position) {
        return (int) (15 + (Math.random() * 20));
    }

    /**
     * @return y 轴下限
     */
    @Override
    public int getMinLevel() {
        return 15;
    }

    /**
     * @return y 轴上限
     */
    @Override
    public int getMaxLevel() {
        return 35;
    }

    /**
     * 设置点上的文字，每个mark是一个，可同时设置点的 8 个方向的文字
     * 注意: Gravity 应使用 CurveView.Gravity 类
     *
     * @param position
     * @return
     */
    @Override
    public Set<CurveView.Mark> onCreateMarks(int position) {
        Set<CurveView.Mark> marks = new HashSet<CurveView.Mark>();
        CurveView.Mark mark = new CurveView.Mark(getLevel(position) + "°", Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 20, 0, 0);
        CurveView.Mark mark1 = new CurveView.Mark(getLevel(position) + "°", Gravity.START | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 20);
        marks.add(mark);
        marks.add(mark1);
        return marks;
    }

    /**
     * 获取第 i 个点 x 轴上的文字
     * @param i
     * @return
     */
    @Override
    public String getXAxisText(int i) {
        return text.substring(i, i + 1);
    }
});

```
