.PHONY: run-dev build-frontend test-frontend test-backend

run-dev: build-frontend
	./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

build-frontend:
	cd frontend && bunx ng build

test-frontend:
	cd frontend && bunx ng test --watch=false $(if $(SPEC),--include $(SPEC))

test-backend:
	./mvnw test $(if $(TEST),-Dtest=$(TEST))
