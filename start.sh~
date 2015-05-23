if ![ "$#" ne 1 ]; then
	echo "Usage ./start.sh splinter|michelangelo"
	exit 1
fi

gnome-terminal --window-with-profile=NOCLOSEPROFILE -e "ssh z3376609@$1"
gnome-terminal --window-with-profile=NOCLOSEPROFILE -e "ssh z3376609@$1"



gnome-terminal --window-with-profile=NOCLOSEPROFILE -e "roslaunch pocketsphinx turtlebot_voice_cmd.launch"

openconnector(){
	export ROS_MASTER_URI=http://$1:11311 
	python ~/catkin_src/connector/listener.py	
}

gnome-terminal --window-with-profile=NOCLOSEPROFILE -e "openconnector"
gnome-terminal --window-with-profile=NOCLOSEPROFILE -e "python ~/catkin_src/connector/talkback.py"
