import sys, os, warnings, ncclient
from ncclient.xml_ import new_ele, sub_ele
from lxml import etree
warnings.simplefilter("ignore", DeprecationWarning)
from ncclient import manager
import xml.etree.ElementTree as ET


edit_config_raw='''<config>\
    <tpa xmlns="http://org/nextworks/qameleon/tpa">
        <tpa-id>1</tpa-id>
        <tpa-direction>ADD</tpa-direction>
        <tpa-number-of-inports>1</tpa-number-of-inports>
        <tpa-number-of-outports>1</tpa-number-of-outports>
        <tpa-ports>
            <port-id>0</port-id>
            <port-direction>I</port-direction>
            <port-frequency-info>
                <base-central-frequency>200000</base-central-frequency>
                <base-central-frequency-granularity>5</base-central-frequency-granularity>
                <central-frequency-slot-granularity>10</central-frequency-slot-granularity>
                <n>2</n>
                <m>2</m>
                <base-central-frequency>20000</base-central-frequency>
                <base-central-frequency-granularity>5</base-central-frequency-granularity>
                <central-frequency-slot-granularity>10</central-frequency-slot-granularity>
                <n>2</n>
                <m>2</m>
            </port-frequency-info>
            <port-id>1</port-id>
            <port-direction>O</port-direction>
        </tpa-ports>
    </tpa>
</config>'''


def demo(host, port, user):
	conn= manager.connect(host=host, port=port, username=user, key_filename="/home/pietro/netconf_keys/key", hostkey_verify=False)

	print("Capabilities")
	for cap in conn.server_capabilities:
		print(cap)

	print("Performing get config");
	response_get_config=conn.get_config(source='running').xml
	print("--------------------------------------------\n"+response_get_config)

	print("Performing get");
	response_get=conn.get(source='running').xml
	print("--------------------------------------------\n"+response_get)

	print("Lock")
	conn.lock()
	print("Performing edit config")
#	print(edit_config_raw)
#	edit_config_result= conn.edit_config(target='running', config=edit_config_raw).xml
#	edit_config_result= conn.edit_config(target='candidate', config=edit_config_raw).xml
#	print("edit config result")
#	print("--------------------------------------------\n"+edit_config_result)
#	print("Commit")
#	conn.commit()
	print("unlock")
	conn.unlock()
#	print("Performing get config");
#	response_get_config=conn.get_config(source='running').xml
#	print(response_get_config)
#	conn.close_session()
	

def main():
	host="127.0.0.1"
	port=830
	demo(host, port,"admin")

main()
