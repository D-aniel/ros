#!/usr/bin/env python

import rospy,time
from move_base_msgs.msg import MoveBaseAction, MoveBaseGoal
import actionlib
from actionlib_msgs.msg import *
from std_msgs.msg import String
import subprocess as subp
from geometry_msgs.msg import Pose, PoseWithCovarianceStamped, Point, Quaternion, Twist
import re
from request import Request

class movement():
	loc = ""
	def movePoint(self,loc):
		move_base = None
		goal = MoveBaseGoal()
		goal.target_pose.header.frame_id = 'map'
		goal.target_pose.header.stamp = rospy.Time.now()
				
		#goal.target_pose.pose = Pose(Point(10.2, -8.79, 0.408), Quaternion(0.000, 0.000, 0.892, -1.500))
		p = Point(10.1, -8.47, 0.408)
		if loc == "desk a":
			p = Point(12.4, -10.5, 0.408)
		elif loc == "desk b":
			p = Point(8.82, -7.9, 0.408)
		elif loc == "door":
			p = Point(11.1, -4.34, 0.408)
		goal.target_pose.pose = Pose(p, Quaternion(0.000, 0.000, 0.892, -1.500))
		#start moving
		self.move_base.send_goal(goal)
		rospy.loginfo("moveforward:Sending goal")
		success = self.move_base.wait_for_result(rospy.Duration(60)) 
		if not success:
			self.move_base.cancel_goal()
			rospy.loginfo("The base failed to reach pose")
		else:
			state = self.move_base.get_state()
			if state == GoalStatus.SUCCEEDED:
				rospy.loginfo("reached pose")
				#starting calli
		rospy.loginfo("Finding goal...")
	
	def moveArm(self,items):
		self.pub.publish(items)	
		rospy.loginfo("Publishing items%s", items)
	
	def armfinish(self, data):
		if data.data == "Free":
			rospy.loginfo("Going to %s", self.loc)
			rospy.loginfo("Bridge finished hearing:%s", data.data)
			self.movePoint(self.loc)
			Request.status = Request.PENDING
					
		
	def bridge(self,data):
		rospy.loginfo("bridge:==%s", data.data)
		Request.status = Request.MOTION
		match = re.match(r'(.*) ([01]+)',data.data)
		#rospy.loginfo("matched: %s",match.group(2))
		rospy.loginfo("Turtlebot Docking")
		self.loc = match.group(1)
		self.movePoint("dock")
		#item = 	list(match.group(2))
		rospy.loginfo("Picking up %s",match.group(2))
		
		self.moveArm(match.group(2))

	
	def init(self):
		self.move_base = actionlib.SimpleActionClient("move_base", MoveBaseAction)
		rospy.loginfo("wait for the action server to come up")
		#self.move_base.wait_for_server(rospy.Duration(2))
		rospy.Subscriber("mvbase", String, self.bridge) 
		rospy.Subscriber("jacofinish", String, self.armfinish)
		self.pub = rospy.Publisher("pickup", String, queue_size=10) 

		


		
