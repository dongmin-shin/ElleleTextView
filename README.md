# ElleleTextView

### 라이브러리 소개
- 해당 라이브러리는 기존의 Wording 단위로 말줄임 되는 TextView를 문자 단위로 말줄임이 되도록 개발한 CustomView 입니다

<br>

### How to
#### Step 1. Add it in your root build.gradle at the end of repositories:
```xml
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

#### Step 2. Add the dependency
```xml
dependencies {
    compile 'com.github.dongmin-shin:ElleleTextView:v0.0.2'
}
```

#### Step 3. Set Android Layout XML
- 기존의 TextView와 동일하게 이용하시면 됩니다.
- 다만, 기본적으로 문장 끝에 말줄임을 지원함으로 android:ellipsize="end"와 같은 설정은 하지 말아야 합니다. (추후 개선 예정)

```XML
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:ellele="http://schemas.android.com/apk/res-auto"
    ... >

    <com.example.acsha.ellipsizetextview.ElleleTextView
            android:id="@+id/sample_textview_3"
            android:layout_width="150dp"
            android:layout_height="wrap_content"
            android:layout_marginBottom="15dp"
            android:background="#ffbbcc"
            android:maxLines="3"
            android:textColor="#000000"
            android:textStyle="bold"
            ellele:removeSpaceFrontOfText="true" />

</LinearLayout>
```

<br>


#### Style Option
- removeSpaceFrontOfText : LineBreak 된 문장 앞에 공백이 존재하는 경우, 공백 제거 여부 (default: false)
- headDrawable : 첫 문장 앞에 이미지를 추가한다. 이미지는 문장의 높이에 맞춰 자동으로 리사이즈 되며, 두 번째 이상의 문장부터는 headImage의 크기와 상관 없이 문장이 시작된다.
- headDrawablePaddingRight : headDrawable과 첫 문장 사이의 Padding
- headDrawableVisibility : headDrawable을 노출 시킬지 여부를 결정한다. (View.Visible / View.Gone, default: visible)

<br>

###removeSpaceFrontOfText 옵션 예제
- 원본 텍스트 : 예술가의 별난 삶에서 찾은 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;예술 창작의 힘으로 살아가&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;는 우리들의 이야기라는 사실을 알고 계시나요?
- removeSpaceFrontOfText (false) : 공백을 LineBreak 계산에 포함시킨다.
```
예술가의 별난 삶에서 찾은
       예술 창작의 힘
으로 살아가는 우리들의 ...
```

<br>

- removeSpaceFrontOfText (true) : LineBreak 한 문장에 공백이 존재하면 공백을 제거한 뒤 LineBreak를 재계산한다.
```
예술가의 별난 삶에서 찾은
예술 창작의 힘으로 살아가
는 우리들의 이야기라는 ...
```

<br>