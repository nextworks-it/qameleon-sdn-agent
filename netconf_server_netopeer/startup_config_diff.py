import xml.etree.ElementTree as ET
import xmltodict, json
import copy

ET.register_namespace("", "urn:cesnet:tmc:datastores:file")
#ET.register_namespace("tpa", "http://org/nextworks/qameleon/tpa")

def get_startup_datastore_file(datastore_filename, yang_model_name):
	f = open(datastore_filename, "r")
	data_store_str=f.read()
	datastore_startup_xml = ET.fromstring(data_store_str)
	datastore_startup_json = xmltodict.parse(data_store_str)		
	#startup_config=datastore_startup_json["datastores"]["startup"][yang_model_name]##TODO if no key found, means no startup config
	#startup_config=datastore_startup_json
	#print(json.dumps(startup_config, indent=4, sort_keys=True))
	return datastore_startup_xml, datastore_startup_json


def get_configuration_from_file(filename):
	print("Pretending to read from "+filename+"configuration file");
	params = {}
	params["tpa-id"]="1"
	params["port-id"]="1"
	params["n"]="10"
	params["m"]="20"
	print("params:")
	print(params)
	return params


	
def find_diff(datastore_startup_xml, params_from_file, object_name):
	#Cases:
	#1. The configuration parameters are equal. No action performed.
	#2. The configuration parameters are different:
		#2.1. No startup datastore 
		#2.2 Different parameter (Generic case)
		#2.3 Not available parameters
		#2.4 Other cases ??
	root=datastore_startup_xml
	found=False
	
	for child in root:
		if "startup" in child.tag:
			startup_root=child

	startup_root_tmp = copy.deepcopy(startup_root)

	for elem in startup_root_tmp.iter():	
		for key in params_from_file.keys():
			str_tmp=elem.tag.split("}")[1]
			if(key == str_tmp and params_from_file[key]==elem.text):
				print("value of "+key+" are equal:"+elem.text)
			if(key == str_tmp and params_from_file[key]!=elem.text):
				print("value of "+key+" are different: XML datastore startup value: "+elem.text+ ". Configuration file value: "+params_from_file[key])
				elem.text=params_from_file[key]


	for elem in startup_root.iter():
		print(elem.tag, elem.text)

	print("...........................")
	for elem in startup_root_tmp.iter():
		print(elem.tag, elem.text)
	
 
	xmlstr = ET.tostring(datastore_startup_xml, method='xml').decode('utf8')
	xmlstr="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+xmlstr
	my_str=xmlstr.replace("ns1:","")
	my_str=my_str.replace("<"+object_name+">","<"+object_name+" xmlns=\"http://org/nextworks/qameleon/"+object_name+"\">")
	my_str=my_str.replace(" xmlns:ns1=\"http://org/nextworks/qameleon/"+object_name+"\"","")
	my_str+="\n"
	text_file = open("datastore-"+object_name+"_tmp.xml", "w")
	n = text_file.write(my_str)
	text_file.close()

params_from_file=get_configuration_from_file("tpa_conf.txt")
datastore_startup_xml, datastore_startup_json=get_startup_datastore_file("datastore-tpa.xml","tpa")
find_diff(datastore_startup_xml, params_from_file,"tpa")

