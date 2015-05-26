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
		#self.p.stdin.flush()
		result = self.p.stdout.readline()
		self.p.stdin.write(self.string+'.')
		#self.p.stdin.flush()
		result = self.p.stdout.readline()
		#process framescript bridge
		
		match = re.match(r'\? : ([a-zA-Z]+) (.*)',result)
		if match:
			if match.group(1) == "checkOrder":
				rospy.loginfo("So you are after %s", match.group(2))
				self.req.currOrder = match.group(2)
			elif match.group(1) == "cancelOrder":
				rospy.loginfo("I must of misheard, what did you want to do again?")
				self.req.currOrder = ""
			elif match.group(1) == "confirmOrder":
				if self.req.currOrder:
					rospy.loginfo("Ok I'll bring %s with me", self.req.currOrder)
					self.req.setOrder()
				elif not self.req.currOrder:
					rospy.loginfo("You haven't told me what to bring")
				self.currOrder = ""
			elif match.group(1) == "checkLocation":
				rospy.loginfo("You're sitting at %s?",match.group(2))
				self.req.currLoc = match.group(2)
			elif match.group(1) == "confirmLocation":
				rospy.loginfo("Ok, I'll note down that you're sitting there")
				self.req.location = self.req.currLoc
			elif match.group(1) == "cancelLocation":
				rospy.loginfo("I must have heard the wrong location, what did want to do again?")
				self.req.currLoc = ""
			elif match.group(1) == "setFavourite":
				rospy.loginfo("Your new favourite is %s", match.group(2))
				self.req.currFav = match.group(2)
			elif match.group(1) == "queryFav":
				if self.req.currFav:
					rospy.loginfo("What did you want me to do with %s", self.req.currFav)
				else:
					rospy.loginfo("I don't know your favourite drink")	
			elif match.group(1) == "getFavourite":
				if self.req.currFav:
					rospy.loginfo("Ok, I'll grab a %s", self.req.currFav)
					self.req.currOrder = self.req.currFav
					self.req.setOrder()
				else:
					rospy.loginfo("I don't know what your favourite is")
					#self.p.stdin.write("setfavourite")
					#result = self.p.stdout.readline()
			elif match.group(1) == "failSafe":
				rospy.loginfo("I dont understand %s",match.group(2))
			elif match.group(1) == "finishOrder":
				if self.req.isReady():
					rospy.loginfo("Ok I'm heading off now")
					rospy.loginfo("%s %s", self.req.location, ''.join(str(o) for o in self.req.order))
					
					msg = self.req.location + " " + ''.join(str(o) for o in self.req.order)
					self.pub.publish(msg)
				elif not self.req.location:
					rospy.loginfo("Where do you want me to take them")
				else:
					rospy.loginfo("You haven't ordered anything")
			rospy.loginfo("matched: %s(%s)",match.group(1),match.group(2))
			
		else:
			rospy.loginfo("unmatched: %s",result)
		
		
		
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
		self.p.stdin.write('Hello.')
		#self.req = request()
		rospy.loginfo("Init hello %s", self.p.stdout.readline())
		#while not rospy.is_shutdown():
			#rospy.loginfo("Listening..")
			#if (self.string):
				#rospy.loginfo("I heard %s", self.string)

		rospy.spin()

if __name__ == '__main__':
    l = listener()
    l.init()
