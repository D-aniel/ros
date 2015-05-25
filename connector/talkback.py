#!/usr/bin/env python

import rospy
from std_msgs.msg import String
from subprocess import Popen, PIPE


class speechpickup:
	def response(self, data):
		#rospy.loginfo(rospy.get_caller_id()+"%s",type(data.data))
	
		rospy.loginfo("talkback.py: %s",data.data)
		#rospy.loginfo(fs)
		#self.pub.publish(data.data)
		self.pub.publish(data.data)
		
	def listener(self):
		rospy.loginfo("speechpickup: preparing to listen")
		rospy.init_node('speechrecog', anonymous=True)
		self.sub = rospy.Subscriber('/recognizer/output', String, self.response)
		#self.pub = rospy.Publisher('speechtofs', String, queue_size=10)
		self.pub = rospy.Publisher('chatter', String, queue_size=10)
		rospy.spin()
	
if __name__ == '__main__':
	s = speechpickup()
	s.listener()
