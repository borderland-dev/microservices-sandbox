package dev.borderland.invoice_management.adapters.output

import dev.borderland.invoice_management.application.ports.InvoicePort
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.waiters.WaiterResponse
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter
import java.util.*

@Component
class DynamoDBInvoiceAdapter : InvoicePort {

    private val dynamoDbClient: DynamoDbClient
    private val tableName = "Invoices"

    init {
        // Initialize DynamoDB client
        dynamoDbClient = DynamoDbClient.builder()
            .build()

        // Ensure table exists or create it
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        val tables = dynamoDbClient.listTables().tableNames()

        if (!tables.contains(tableName)) {
            val request = CreateTableRequest.builder()
                .tableName(tableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .provisionedThroughput(
                    ProvisionedThroughput.builder()
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build()
                )
                .build()

            val response = dynamoDbClient.createTable(request)

            // Wait for table to be created
            val waiter: DynamoDbWaiter = dynamoDbClient.waiter()
            val describeTableRequest = DescribeTableRequest.builder()
                .tableName(tableName)
                .build()

            waiter.waitUntilTableExists(describeTableRequest)
        }
    }

    override fun createInvoice(number: String, amount: Double, dueDate: String): String {
        val id = UUID.randomUUID().toString()

        val itemValues = mutableMapOf<String, AttributeValue>()
        itemValues["id"] = AttributeValue.builder().s(id).build()
        itemValues["number"] = AttributeValue.builder().s(number).build()
        itemValues["amount"] = AttributeValue.builder().n(amount.toString()).build()
        itemValues["dueDate"] = AttributeValue.builder().s(dueDate).build()

        val request = PutItemRequest.builder()
            .tableName(tableName)
            .item(itemValues)
            .build()

        dynamoDbClient.putItem(request)
        return id
    }

    override fun getInvoiceById(id: String): String {
        val keyToGet = mutableMapOf<String, AttributeValue>()
        keyToGet["id"] = AttributeValue.builder().s(id).build()

        val request = GetItemRequest.builder()
            .tableName(tableName)
            .key(keyToGet)
            .build()

        val retrievedItem = dynamoDbClient.getItem(request).item()

        if (retrievedItem.isNotEmpty()) {
            // Convert the item to a JSON string (simplified approach)
            return """
                {
                    "id": "${retrievedItem["id"]?.s()}",
                    "number": "${retrievedItem["number"]?.s()}",
                    "amount": ${retrievedItem["amount"]?.n()},
                    "dueDate": "${retrievedItem["dueDate"]?.s()}"
                }
            """.trimIndent()
        }
        throw NoSuchElementException("Invoice with id $id not found")
    }

    override fun updateInvoice(id: String, number: String, amount: Double, dueDate: String): String {
        val itemKey = mutableMapOf<String, AttributeValue>()
        itemKey["id"] = AttributeValue.builder().s(id).build()

        val updatedValues = mutableMapOf<String, AttributeValueUpdate>()
        updatedValues["number"] = AttributeValueUpdate.builder()
            .value(AttributeValue.builder().s(number).build())
            .action(AttributeAction.PUT)
            .build()
        updatedValues["amount"] = AttributeValueUpdate.builder()
            .value(AttributeValue.builder().n(amount.toString()).build())
            .action(AttributeAction.PUT)
            .build()
        updatedValues["dueDate"] = AttributeValueUpdate.builder()
            .value(AttributeValue.builder().s(dueDate).build())
            .action(AttributeAction.PUT)
            .build()

        val request = UpdateItemRequest.builder()
            .tableName(tableName)
            .key(itemKey)
            .attributeUpdates(updatedValues)
            .returnValues(ReturnValue.ALL_NEW)
            .build()

        val response = dynamoDbClient.updateItem(request)

        // Return the updated item as a JSON string
        return """
            {
                "id": "${response.attributes()["id"]?.s()}",
                "number": "${response.attributes()["number"]?.s()}",
                "amount": ${response.attributes()["amount"]?.n()},
                "dueDate": "${response.attributes()["dueDate"]?.s()}"
            }
        """.trimIndent()
    }

    override fun deleteInvoice(id: String): Boolean {
        val keyToDelete = mutableMapOf<String, AttributeValue>()
        keyToDelete["id"] = AttributeValue.builder().s(id).build()

        val request = DeleteItemRequest.builder()
            .tableName(tableName)
            .key(keyToDelete)
            .build()

        try {
            dynamoDbClient.deleteItem(request)
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
