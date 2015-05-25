#!/usr/bin/env python
import rospy,time
from move_base_msgs.msg import MoveBaseAction, MoveBaseGoal
import actionlib
from actionlib_msgs.msg import *
from std_msgs.msg import String
import subprocess as subp
from geometry_msgs.msg import Pose, PoseWithCovarianceStamped, Point, Quaternion, Twist
from movebase import movement
import re

class listener():
	string = ""
	move = movement()
	def callback(self,data):

		self.string = data.data
		#rospy.loginfo("callbackheard: %s", data.data)
		self.p.stdin.write(self.string+'.')
		#self.p.stdin.flush()
		result = self.p.stdout.readline()
		self.p.stdin.write(self.string+'.')
		#self.p.stdin.flush()
		result = self.p.stdout.readline()
		#process framescript bridge
		
		match = re.match(r'\? : (.*) (.*)',result)
		if match:
			rospy.loginfo("matched: %s(%s)",match.group(1),match.group(2))
		else:
			rospy.loginfo("unmatched: %s",result)
		
		#self.pub.publish(result)
		#rospy.loginfo("init finished %s: %s", self.string,result)
			
	def init(self):
		rospy.init_node('framescriptlistener', anonymous=False)		
		rospy.Subscriber("chatter", String, self.callback)
		self.pub = rospy.Publisher("mvbase", String, queue_size=10)
		#self.cancelpub = rospy.Publisher('/move_base/cancel', actionlib_msgs/GoalID, queue_size=10)
		self.move.init()
		self.string = ""
		rospy.loginfo("Listening...")
		cmd = "java -cp mica/framescript.jar unsw.cse.framescript.FrameScript mica/scripts/grammar.frs mica/scripts/bot.frs".split()
		self.p = subp.Popen(cmd, stdin=subp.PIPE,stdout=subp.PIPE)
		#rospy.loginfo("init: %s",p.stdout.readline()) #ignore Framescript init text
		self.currLocation = ""
		self.currOrder = ""
		self.p.stdout.readline()
		self.p.stdin.write('Hello.')
		rospy.loginfo("Init hello %s", self.p.stdout.readline())
		#while not rospy.is_shutdown():
			#rospy.loginfo("Listening..")
			#if (self.string):
				#rospy.loginfo("I heard %s", self.string)

		rospy.spin()

if __name__ == '__main__':
    l = listener()
    l.init()
