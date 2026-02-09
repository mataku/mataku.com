---
title: Sonatype OSSRH のアクセストークン認証対応
date: 2024-06-26T13:41:45+09:00
tags:
---

Maven Central へバイナリをあげるにあたり、Sonatype OSSRH へバイナリをあげようとしたところ、Content access is protected by token のエラーが出ていたので、[https://support.sonatype.com/hc/en-us/articles/360049469534-401-Content-access-is-protected-by-token-when-accessing-repositories](https://support.sonatype.com/hc/en-us/articles/360049469534-401-Content-access-is-protected-by-token-when-accessing-repositories) を参考に対応した。

https://s01.oss.sonatype.org/ へ行き、右上の Profile からアクセストークンを発行すると username:token という形式で User Token が発行されるので、それぞれ username, password として差し替える。

![](/images/sonatype-navigation.png "アクセストークン発行画面への道")

![](/images/sonatype-generate-token.png "アクセストークン発行画面")

<!-- {{< figure src="../navigation.png" align=center caption="アクセストークン発行画面への道">}} -->

<!-- {{< figure src="../generate_token.png" align=center caption="アクセストークン発行画面">}} -->


値を参照する側は特に変わらないはず。

```kotlin
// maven publish を用いた例
extensions.configure<PublishingExtension>() {
  repositories {

    maven {
      name = "Release"
      val releasesRepoUrl =
        uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
      url = releasesRepoUrl
      credentials {
        username =
          System.getenv("OSSRH_USERNAME") ?: rootProject.extra["ossrhUsername"] as String
        password =
          System.getenv("OSSRH_PASSWORD") ?: rootProject.extra["ossrhPassword"] as String
      }
    }
// ...
```

僕は手元では local.properties、CI では環境変数から読むようにしていたので local.properties にそれぞれ値を差し替えた。

```properties
// local.properties
ossrhUsername=username
ossrhPassword=token
```

- - - 

別で Maven Central のアカウントマイグレーションのメールが来ていて、当初はこれ関連かなと勘違いしそうだったがサポートページを幸運にも見つけたので助かった。
