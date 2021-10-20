[![](https://jitpack.io/v/ttzt777/Android-SpanTextView.svg)](https://jitpack.io/#ttzt777/Android-SpanTextView)
## 引用方式
- 在项目根目录的build.gradle文件中添加
```groovy
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```
- 对应module添加依赖
```groovy
    dependencies {
        implementation 'com.github.ttzt777.Android-SpanTextView:span-kernel:1.0.0'
        implementation 'com.github.ttzt777.Android-SpanTextView:span-collapsed:1.0.0'
    }
```
### **span-kernel**
- 支持TextView中Span的点击事件处理，无需设置MovementMethod
- 优化Span点击逻辑，修复末尾时点击空白区域会判定到最后一个Span的问题
- 提供自定义点击Span，移除下划线，支持默认点击更改背景色
- 提供工具类移除CharSequence中的半个emoj表情
### **span-collapsed**
- 支持文本折叠和普通显示
- 支持在末尾显示展开全文且无点击事件
- 支持传入固定宽度和自动Layout后获取宽度
- 支持最大显示行数设置及折叠后显示的行数
- 支持自定义展开和收起的文本、颜色、点击后背景颜色