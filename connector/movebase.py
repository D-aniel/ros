#!/usr/bin/env python

import rospy,time
from move_base_msgs.msg import MoveBaseAction, MoveBaseGoal
import actionlib
from actionlib_msgs.msg import *
from std_msgs.msg import String
import subprocess as subp
from geometry_msgs.msg import Pose, PoseWithCovarianceStamped, Point, Quaternion, Twist

class movement():

	def moveforward(self):
		#we'll send a goal to the robot to tell it to move to a pose that's near the docking station
		goal = MoveBaseGoal()
		goal.target_pose.header.frame_id = 'map'
		goal.target_pose.header.stamp = rospy.Time.now()
		goal.target_pose.pose = Pose(Point(1, 1, 0.000), Quaternion(0.000, 0.000, 0.892, -1.500))

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
	
	def bridge(self,data):
		rospy.loginfo("Bridge heard:%s", data.data)
		if data.data == "? moveForward ( )\n":
			rospy.loginfo("Calling moveForward")
			self.moveforward()
		elif data.data == "stop":	
			self.move_base.cancel_goal()
			rospy.loginfo("Cancelling goal")
		
	
	def init(self):
		self.move_base = actionlib.SimpleActionClient("move_base", MoveBaseAction)
		rospy.loginfo("wait for the action server to come up")
		self.move_base.wait_for_server(rospy.Duration(2))
		rospy.Subscriber("mvbase", String, self.bridge) 


		
