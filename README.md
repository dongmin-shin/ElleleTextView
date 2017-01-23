# ElleleTextView

>#### 라이브러리 소개
- 해당 라이브러리는 기존의 Wording 단위로 말줄임 되는 TextView를 문자 단위로 말줄임이 되도록 구성 된 CustomView 입니다


>#### 사용법
- 기존의 TextView와 동일하게 이용하시면 됩니다
- 다만, 기본적으로 말줄임을 지원하고 있기 때문에 android:ellipsize="end"와 같은 설정 처리하지 않으셔도 됩니다.

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

>#### Style 소개
- removeSpaceFrontOfText : LineBreak 된 문장 앞에 공백이 존재하는 경우, 공백을 제거 여부 (default : false)

###removeSpaceFrontOfText 옵션 예제
- 원본 텍스트 : 예술가의 별난 삶에서 찾은 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;예술 창작의 힘으로 살아가&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;는 우리들의 이야기라는 사실을 알고 계시나요?
- removeSpaceFrontOfText (false) : 공백을 LineBreak 계산에 포함시킨다.
```
예술가의 별난 삶에서 찾은
       예술 창작의 힘
으로 살아가는 우리들의 ...
```

- removeSpaceFrontOfText (true) : LineBreak 한 문장에 공백이 존재하면 공백을 제거한 뒤 LineBreak를 재계산한다.
```
예술가의 별난 삶에서 찾은
예술 창작의 힘으로 살아가
는 우리들의 이야기라는 ...
```