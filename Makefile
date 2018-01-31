.DEFAULT_GOAL := dev

.PHONY: dev
dev:
	@molecule converge
