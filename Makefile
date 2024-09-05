download:
	curl -L https://istio.io/downloadIstio | ISTIO_VERSION=1.23.0 sh -
	export PATH="$PATH:/Users/farazahmed/IdeaProjects/istio/istio-1.23.0/bin"
setup:
	istioctl profile list
	istioctl install --set profile=demo -y
	istioctl install -f settings.yaml -y
	kubectl create namespace istio-demo
circuit:
	kubectl apply -f circuitbreaker.yaml

rollback:
	kubectl delete -f circuitbreaker.yaml