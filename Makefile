build:
# 	docker image rm imranq2/cql-evaluator-local || echo "no image"
	docker build -t imranq2/cql-evaluator-local .

shell:
	docker run -it imranq2/cql-evaluator-local sh

up:
	docker build -t imranq2/cql-evaluator-local . && \
	docker run --name cql-evaluator --rm iimranq2/cql-evaluator-local

tests:
