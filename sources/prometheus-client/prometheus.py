import requests
import sys
from onesaitplatform.iotbroker import DigitalClient
from datetime import datetime
import os
import logging


def timestamp_to_datetime(timestamp):
    """Generate datetime string formatted for the ontology timeseries

    Args:
        timestamp (float, timemstamp): Timestamp value as float

    Returns:
        str: Returns the timestamp with the format necessary to insert in the Ontology
    """
    return datetime.fromtimestamp(timestamp).strftime("%Y-%m-%dT%H:%M:%SZ")


def get_metric_names(url):
    """Get ALL Metric names from prometheus

    Args:
        url (str): Prometheus url

    Returns:
        list: List with all the metric names

    """
    response = requests.get(f'{url}/api/v1/label/__name__/values')
    data = response.json()['data']
    return data


def get_metric_data(url, metric):
    """Get metric data from Prometheus

    Args:
        url (str): Prometheus URL
        metric (str): Name of the metric

    Returns:
        dict: Dictionary for the metric data

    """
    response = requests.get(f'{url}/api/v1/query', params={"query": metric})
    data = response.json()['data']
    return data

def format_metric(metric_value):
    """Formatic metrics

    Args:
        metric_value (dict): Metric dictionary, requiered keys: timestmap, metric, job, instance

    Returns:
        dict: Dictionary with the following format\n{
                      "TimeSerie": {
                        "timestamp": {
                          "$date": ""
                        },
                        "metric": "",
                        "job": "",
                        "instance": "",
                        "value": 0
                      }
                    }

    """
    data = {
        "TimeSerie": {
            "timestamp": {
                "$date": timestamp_to_datetime(metric_value["value"][0])
            },
            "metric": metric_value["metric"]["__name__"],
            "job": metric_value["metric"]["job"],
            "instance": metric_value["metric"]["instance"],
            "image": "",
            "id": "",
            "name": "",
            "value": float(metric_value["value"][1])
        }
    }
    if "image" in metric_value["metric"]:
        data["TimeSerie"]["image"] = metric_value["metric"]["image"]
    if "id" in metric_value["metric"]:
        data["TimeSerie"]["id"] = metric_value["metric"]["id"]
    if "name" in metric_value["metric"]:
        data["TimeSerie"]["name"] = metric_value["metric"]["name"]
    return data


def insert_metric(data):
    logger = logging.getLogger(__name__)
    """Inserting metrics on the ontologies

    Args:
        data (list[dict]): Metrics data to insert in the Ontology

    Returns:

    Example:
        insert_metric([{
                      "TimeSerie": {
                        "timestamp": {
                          "$date": ""
                        },
                        "metric": "",
                        "job": "",
                        "instance": "",
                        "value": 0
                      }
                    },....])


    """
    digcli = DigitalClient(host=os.environ.get('OP_SERVER_IP'), iot_client=os.environ.get("DC_NAME"), iot_client_token=os.environ.get("DC_TOKEN"))

    digcli.avoid_ssl_certificate = True

    digcli.connect()
    ok_request, res_request = digcli.insert("PrometheusMetricOntology", data)
    logger.info(ok_request, res_request)
    digcli.leave()


if __name__ == '__main__':
    """
	    Main code which uses the functions declared to generate the data extracted from Prometheus to insert as an ontology with the digital client
    TODO:
    Use the PrometheusMetricTimestamp ontology to keep track of the last timestamp read of each measurement, so you don't need to extract all data each time
    """
    url = f"http://{os.environ.get('PR_HOST')}"
    metrics = get_metric_names(url)
    for metric in metrics:
        metric_data = get_metric_data(url, metric)
        if metric_data:
            metric_input = []
            for metric_value in metric_data["result"]:
                metric_input.append(format_metric(metric_value))
            if metric_input:
                insert_metric(metric_input)





