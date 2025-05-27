package dev.borderland.invoice_management

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InvoiceManagementApplication

fun main(args: Array<String>) {
	runApplication<InvoiceManagementApplication>(*args)
}
