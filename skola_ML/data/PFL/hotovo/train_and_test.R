library(rpart)
library(e1071)
library(adabag)

type <- commandArgs(trailingOnly = TRUE)[1];
word <- commandArgs(trailingOnly = TRUE)[2];
train_table<-read.table("train_data")
test_table<-read.table("test_data")


train_table[, "semantic_class"] =
        factor(train_table[,"semantic_class"]);
levels = levels(train_table[, "semantic_class"])

test_table_copy = test_table;

test_table_copy[, "semantic_class"] = 
    factor(levels[0], levels=levels)

if (type == "bagging") {
    if (word=="ally") { 
        classifier <- bagging("semantic_class ~ .", train_table,
                               mfinal = 10,
                               control = rpart.control(
                                    cp = 0.01,
                                    minsplit = 50
                               ));
    } else {
        classifier<-bagging("semantic_class ~ .", train_table);
    }

    found_classes <- predict.bagging(classifier,
                        newdata = test_table_copy);
} else {
    if (word=="plough") {
        control = rpart.control(
            cp = as.numeric(0.01),
            minsplit = as.numeric(50)
        )
        classifier<-boosting(
            "semantic_class ~ .",
            data = train_data, 
            mfinal = as.numeric(0.01),
            boos = TRUE,
            coeflearn = "Breiman");

    } else {
        classifier<-boosting("semantic_class ~ .", train_table);
    }

    found_classes <- predict.boosting(classifier,
                        newdata = test_table_copy);
}

found_classes <- found_classes$class;
correct_classes <- test_table[, 1]

same <- found_classes == correct_classes

correctness<- length(same[same])
        
res <- (correctness/length(same))

found_classes;

res
