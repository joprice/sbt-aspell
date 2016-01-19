
lazy val Native = config("native").extend(Compile)

configs(Native)

name := "sbt-aspell"

organization := "com.joprice.sbt"

sbtPlugin := true

publishArtifact in Test := false

scalaVersion := "2.10.5"

bintrayOrganization := Some("joprice")

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

ScriptedPlugin.scriptedSettings

scriptedSettings

scriptedBufferLog := false

scriptedLaunchOpts ++= Seq("-Xmx2G", "-Dplugin.version=" + version.value)

scalacOptions := Seq(
  "-unchecked", 
  "-deprecation", 
  "-feature", 
  "-encoding", 
  "utf8"
)

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.lucidchart" %% "lucid-aspell" % "2.1.1-SNAPSHOT" classifier "" classifier "native-x86_64",
  //"com.lucidchart" %% "lucid-aspell" % "2.1.0-SNAPSHOT-native-x86_64" classifier "x86_64",
  "org.scalanlp" %% "nak" % "1.2.1"
)

