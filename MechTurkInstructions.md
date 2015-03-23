# Introduction #

Running a Turk study (or just generally getting data through the web) is fairly easy with this code. You need to create an applet and put it on the web, set up data collection, and then have people interact with the applet on their computers. Below are instructions for running a study by Mechanical Turk; the instructions can easily be adapted for other data-gathering methods.



# Details #

### 1. Create a class that extends RLApplet (for display of the agent-environment interaction only) or TamerApplet (for human interaction with the agent). ###

### 2. Pack the code (this project plus your additions) into a jar file. ###

In Eclipse, this can be done as follows. First, right-click on the project your class file is in and choose Export. Then choose Jar file and click Next. In the Jar Export window, choose a file name and location. I'll assume tamer.jar is the name in the steps below. Then you should be able to use the default settings and click Finish.

### 3. Place the jar file on a publicly readable web server. ###

### 4. Write html code that creates the applet from the intended class. ###

**Example:**
```
<table>
<tr> <td bgcolor="black">
<applet code="edu.utexas.cs.tamerProject.applet.TamerApplet.class"
        archive="<URL here>/tamer.jar"
        width=600 height=600>
  <PARAM name="classloader_cache" value="false">
  <PARAM name="separate_jvm" value="true">
  <!--<param name="identifier" value="${hit_id}">-->
  <param name="domain" value="loopmaze">
  <param name="agent" value="control">
  <param name="isHIT" value="true">
  <param name="numInTaskSeq" value="1">
  <param name="fullLog" value="true">
  <param name="rewLog" value="true">
  <param name="numEpisodes" value="3">
  <!-- <param name="maxTotalSteps" value="40"> -->
  <param name="speedControls" value="false">
  <param name="singleStepControl" value="false">
  <param name="speed" value="300">
  <!--<param name="timeRequirement" value="85">-->
  <param name="trainingControl" value="false">
</applet>
</td>
<td width="500">
<font face="Courier" size="3"><b>
<b>To play the game, move Kermitbot to water with the arrow keys.</b>
<br><br>
<font color="992222"><b>***The game window only receives your keystrokes once you have double-clicked on it.***</b></font>
</font>
</td> </tr>
</table>
```

### 5. Create a Turk task that uses this html (either directly loading your applet or by linking to your website, where the applet would be). ###

### 6. Set up a data collection method that sends log information from participants' computers to your server. ###

This codebase already has support for sending log data to a server running PHP. See RLApplet's method sendStringToPHP(). I'm happy to share that PHP code if you're interested.

### 7. Run the Turk task. ###

Instructions for being a "requester" on Mechanical Turk are available across the web, including on their site.