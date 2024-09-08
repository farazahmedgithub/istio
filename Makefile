download:
	curl -L https://istio.io/downloadIstio | ISTIO_VERSION=1.23.0 sh -
	export PATH="$PATH:/Users/farazahmed/IdeaProjects/istio/istio-1.23.0/bin"
setup:
	istioctl profile list
	istioctl install --set profile=demo -y
	istioctl install -f settings.yaml -y
	kubectl create namespace istio-demo
	kubectl label namespace istio-demo istio-injection=enabled --overwrite

circuit:
	kubectl apply -f circuitbreaker.yaml

rollback:
	kubectl delete -f circuitbreaker.yaml
	kubectl delete -f deployment.yaml

build-client:
	cd demo-client && mvn clean install -DskipTests && docker build -t demo-client:istio .

build-server:
	cd demo-server && mvn clean install -DskipTests && docker build -t demo-server:istio .

deploy:
		kubectl apply -f deployment.yaml

port:
	kubectl -n istio-demo port-forward service/client1 8082:8080
server-port:
	kubectl -n istio-demo port-forward service/myserver 8081:8080

logs:
	kubectl -n istio-demo logs -f service/client1
server-logs:
	kubectl -n istio-demo logs -f service/myserver
