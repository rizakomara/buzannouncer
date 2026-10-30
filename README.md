# buzannouncer
Android-based software for Public Transport Announcer using Car Android Head Unit.
Please use Android Studio to compile and deploy.

<i>Capabilities:</i>
1. Automatic Bus stop Voice Announcer based on GPS Coordinates
2. Custom Voice Announcement
3. GPS Tracking Capability, when your Head Unit have <b>4G LTE</b> capability.
4. Integrate with other equipment's such as <b>Running Text Display</b> and <b>Integrated Media Player</b>, using <b>RS485</b> communication.

<i>Preparation:</i>
1. Android Car Head Unit, please choose minimum <b>4GB RAM</b>, you can choose 7 inch, 9 inch or 10 inch touch screen display. <b>4G LTE</b> features will be an advantage, you can make your Head Unit as GPS Tracker also!
2. A CH340 USB to Serial/RS485 for other equipment integration (optional)
3. We recommend using <b>File Manager+</b> to manipulate files on your Head Unit.
4. By using <b>File Manager+</b>, create <b>obuconfig</b> folder at root of Head Unit's Internal Storage
5. Copy <b>config.xml, customvoice.xml, pisdata.txt</b> and <b>routelist.txt</b> to <b>obuconfig</b> folder
6. Create <b>mp3 files</b> for Voice Announcement and store the files at Music folder of your Head Unit

<i>Integration to other equipments:</i>
The simplest ways to integrate with other equipment is using RS-485 communication. RS-485 communication allows many devices connected using only two wires. Please use USB to RS485 converter connected to USB Ports of your Head Unit.
RS485 format (2400,8N1)
$PIS,[routecode],[routename],[time],[position],[mutestate],[buscode],[datetime],[voicetoplay],[overspeedalert],[routedelimiter],[busstop],[ledfrontmode],[ledsidemode],[ledindoormode],[ledcodecolor],[lednamecolor],[ledbusstopcolor],[ledcoderoutebgcolor],*
This data will be sent every 1 second.

Examples:
For outdoor running text display, you can only parse [routecode] and [routename] fields.
For indoor running text display, you may need to parse [routecode] and [position] fields only.




