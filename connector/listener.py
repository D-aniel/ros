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
from request import Request

class listener():
	string = ""
	move = movement()
	req = Request()
	
	
	def callback(self,data):
		if self.req.status == Request.MOTION:
			rospy.loginfo("I'm busy at the moment, going to %s",self.req.currLoc)
			return
			
		self.string = data.data
		#rospy.loginfo("callbackheard: %s", data.data)
		self.p.stdin.write(self.string+'.')
		self.p.stdin.flush()
		result = self.p.stdout.readline()
		self.p.stdin.write(self.string+'.')
		self.p.stdin.flush()
		result = self.p.stdout.readline()
		#process framescript bridge
		
		match = re.match(r'\? : ([a-zA-Z]+) ({door|desk a|desk b|desk c|coke|lemonade|water}) (.*)',result)
		if match:
			if match.group(1) == "FINISH":
				rospy.loginfo("%s", match.group(3))
				self.req.currLoc = match.group(2)
				msg = match.group(2) + " " + ''.join(str(o) for o in self.req.order)
				self.pub.publish(msg)
				self.req.status = Request.MOTION
			elif match.group(1) == "ORDER":
				rospy.loginfo("Adding %s to order",match.group(2))
				self.req.setOrder(match.group(2))
				rospy.loginfo("%s",match.group(3))
				#self.req.currOrder = ""
			
		else:
			rospy.loginfo("Framescript: %s",result)
		
		
		
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
		#cmd = "java -cp mica/framescript.jar unsw.cse.framescript.FrameScript mica/scripts/ordering.frs".split()
		self.p = subp.Popen(cmd, stdin=subp.PIPE,stdout=subp.PIPE)
		#rospy.loginfo("init: %s",p.stdout.readline()) #ignore Framescript init text
		self.p.stdout.readline()
		self.p.stdin.write('SETFAV none.')
		rospy.loginfo("Init %s", self.p.stdout.readline())
		self.p.stdin.write('SETLOC none.')
		rospy.loginfo("Init %s", self.p.stdout.readline())
		self.p.stdin.write('SETLOC none.')
		rospy.loginfo("Init %s", self.p.stdout.readline())
		#while not rospy.is_shutdown():
			#rospy.loginfo("Listening..")
			#if (self.string):
				#rospy.loginfo("I heard %s", self.string)

		rospy.spin()

if __name__ == '__main__':
    l = listener()
    l.init()
