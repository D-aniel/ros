#!/usr/bin/env python
import rospy,time
from move_base_msgs.msg import MoveBaseAction, MoveBaseGoal
import actionlib
from actionlib_msgs.msg import *
from std_msgs.msg import String
import subprocess as subp
from geometry_msgs.msg import Pose, PoseWithCovarianceStamped, Point, Quaternion, Twist
from movebase import movement

class listener():
	string = ""
	move = movement()
	def callback(self,data):

		rospy.loginfo("I heard %s", data.data)
		self.string = data.data
		#if data.data == "forward":
		#	rospy.loginfo("moveFoward()")
		#	self.move.moveforward()
		#elif data.data == "stop":
		#	self.move.move_base.cancel_goal()
		
		
			
	def init(self):
		rospy.init_node('framescriptlistener', anonymous=False)		
		rospy.Subscriber("chatter", String, self.callback)
		self.pub = rospy.Publisher("mvbase", String, queue_size=10)
		self.cancelpub = rospy.Publisher('/move_base/cancel', actionlib_msgs/GoalID, queue_size=10)
		self.move.init()
		rospy.loginfo("Listening...")
		cmd = "java -cp mica/framescript.jar unsw.cse.framescript.FrameScript mica/scripts/grammar.frs mica/scripts/bot.frs".split()
		p = subp.Popen(cmd,stdin=subp.PIPE,stdout=subp.PIPE)
		p.stdout.readline() #ignore Framescript init text
		
		while not rospy.is_shutdown():
			while (self.string):
				p.stdin.write(self.string+'.')
				#print(p.stdout.readline())
				result = p.stdout.readline()
				#rospy.loginfo("Framescript: %s", result)
				if self.string == "stop":
					rospy.loginfo("Stoping")
					self.cancelpub.publish('{}')
				else:
					self.pub.publish(result)
				self.string = ""
		#rospy.spin()

if __name__ == '__main__':
    l = listener()
    l.init()
