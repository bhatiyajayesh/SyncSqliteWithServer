<?php
$user_name="root";
$password="";
$server="localhost";
$db_name="contactdb";

$con=mysqli_connect($server,$user_name,$password,$db_name);

if($con)
{
	$Name=$_POST['name'];
	$query="insert into contacts(name) values('".$Name."');";
	$result=mysqli_query($con,$query);
	
	if($result)
	{
		$status='OK';
	}
	else
	{
		$status='FAILED';
	}
}
else{
	$status='FAILED';
}

echo json_encode(array("response"=>$status));

mysqli_close($con);
?>