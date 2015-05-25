#!/usr/bin/env python

import rospy,time
from move_base_msgs.msg import MoveBaseAction, MoveBaseGoal
import actionlib
from actionlib_msgs.msg import *
from std_msgs.msg import String
import subprocess as subp
from geometry_msgs.msg import Pose, PoseWithCovarianceStamped, Point, Quaternion, Twist
import re

class movement():
		
	def movePoint(self,loc):
		move_base = None
		goal = MoveBaseGoal()
		goal.target_pose.header.frame_id = 'map'
		goal.target_pose.header.stamp = rospy.Time.now()
		
		dock = Point(10.2, -8.79, 0.408)
		door = Point(11.1, -4.34, 0.408)
		deska = Point(12.4,-10.5, 0.408)
		deskb = Point(8.82,-7.9, 0.408)
		
		#goal.target_pose.pose = Pose(Point(10.2, -8.79, 0.408), Quaternion(0.000, 0.000, 0.892, -1.500))
		goal.target_pose.pose = Pose(Point(11.4, -4.34, 0.408), Quaternion(0.000, 0.000, 0.892, -1.500))
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
	
	def bridge(self,data):
		rospy.loginfo("bridge:==%s", data.data)
		match = re.match(r': (.*) (.*)',data.data)
		#rospy.loginfo("matched: %s",match.group(2))
		
		if data.data == "moveForward":
			rospy.loginfo("Calling moveForward")
			#new thread
			#self.moveforward()
		elif match.group(1) == "pickup":
			rospy.loginfo("Picking up %s",match.group(2))
			self.pub.publish(match.group(2))
		elif data.data == "saveFavourite(^2)":
			rospy.loginfo("saveFavourite")
		rospy.loginfo("Bridge finished hearing:%s", data.data)
			
	
	def init(self):
		self.move_base = actionlib.SimpleActionClient("move_base", MoveBaseAction)
		rospy.loginfo("wait for the action server to come up")
		#self.move_base.wait_for_server(rospy.Duration(2))
		rospy.Subscriber("mvbase", String, self.bridge) 
		self.pub = rospy.Publisher("pickup", String, queue_size=10) 
		self.favourite = ""
		self.user = 0

		


		
