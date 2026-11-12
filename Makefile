.PHONY: run-dev test-frontend

run-dev:
	./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

test-frontend:
	cd frontend && bunx ng test --watch=false $(if $(SPEC),--include $(SPEC))
