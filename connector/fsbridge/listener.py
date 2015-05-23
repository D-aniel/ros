#!/usr/bin/env python
import rospy,time
from std_msgs.msg import String
import subprocess as subp
string = ""
def callback(data):
	global string
	rospy.loginfo("I heard %s", data.data)
	string = data.data
    
def listener():
	global string
	rospy.init_node('framescriptlistener', anonymous=False)
	rospy.Subscriber("chatter", String, callback)


	rospy.loginfo("Waiting...")
	cmd = "java -cp ../mica/framescript.jar unsw.cse.framescript.FrameScript ../mica/scripts/museum.frs ../mica/scripts/bot.frs".split()
	p = subp.Popen(cmd,stdin=subp.PIPE,stdout=subp.PIPE)
	p.stdout.readline() #ignore Framescript init text
	while not rospy.is_shutdown():
		while (string):
			p.stdin.write(string+'.')
			print(p.stdout.readline())
			string = ""
		
	#rospy.spin()


if __name__ == '__main__':
    listener()
