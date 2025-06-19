package com.example.ladycure.presentation.applications

import BabyBlue
import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import Green
import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import android.net.Uri
import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.material3.TextButton
import coil.compose.AsyncImage
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.presentation.register.components.DatePickerButton
import com.example.ladycure.utility.SnackbarController
import com.example.ladycure.utility.rememberImagePickerLauncher
import java.time.LocalDate

@Composable
fun DoctorApplicationScreen(
    navController: NavController,
    snackbarController: SnackbarController
) {
    val viewModel: DoctorApplicationViewModel = viewModel()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var showSuccessDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 for personal, 1 for professional

    // Show error messages
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { error ->
            snackbarController.showMessage(error)
            viewModel.errorMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DefaultBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Doctor Application",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DefaultPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = {
                    val intent = Intent(
                        ACTION_SENDTO,
                        "mailto:ladycure.help@gmail.com".toUri()
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .padding(4.dp)
                ) {
                    Text(
                        "Any Questions? Contact Us!",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    )
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Contact Us",
                        tint = Color.Gray,
                    )

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()

                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabButton(
                    text = "Personal Info",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Professional Info",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            // Application Form Card
            Card(
                modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = DefaultOnPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tab Content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (selectedTab) {
                            0 -> PersonalInfoTab(viewModel, onFinished = {
                                focusManager.clearFocus()
                                selectedTab = 1
                            })

                            1 -> ProfessionalInfoTab(viewModel)
                        }
                    }
                }
            }

            // Submit Button (fixed at bottom)
            Button(
                onClick = {
                    viewModel.hasSubmitted = true
                    if (viewModel.validateApplication()) {
                        viewModel.submitApplication(
                            onSuccess = {
                                snackbarController.showMessage("Application submitted successfully!")
                                showSuccessDialog = true
                            },
                            onError = { error ->
                                snackbarController.showMessage(error)
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Fill remaining space
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.isFormValid()) DefaultPrimary else Color.Gray,
                    contentColor = Color.White
                ),
                enabled = viewModel.isFormValid()
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Submit Application",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Progress Indicator
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { /* Prevent clicks behind */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Submitting Application",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Step indicator
                    Text(
                        text = "Step ${viewModel.currentStep} of ${viewModel.totalSteps}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Linear progress with percentage
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(
                            progress = { viewModel.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = DefaultPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.progressText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when (viewModel.currentStep) {
                            1 -> "Creating your account..."
                            2 -> "Uploading license photo..."
                            3 -> "Uploading diploma..."
                            4 -> "Submitting application..."
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = { showSuccessDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Success icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Green.copy(alpha = 0.1f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Green,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Application Submitted",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BabyBlue,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your application has been submitted successfully. We will review it and get back to you soon. You can log in to view the status of your application.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DefaultOnPrimary.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            showSuccessDialog = false
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(48.dp)
                    ) {
                        Text(
                            "Great!",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, DefaultPrimary),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) DefaultPrimary else Color.Transparent,
            contentColor = if (isSelected) Color.White else DefaultPrimary,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Text(text)
    }
}

@Composable
private fun PersonalInfoTab(
    viewModel: DoctorApplicationViewModel,
    onFinished: () -> Unit
) {

    val focusManager = LocalFocusManager.current

    val (emailFocus, firstNameFocus, lastNameFocus, passwordFocus, confirmPasswordFocus) = remember {
        List(5) { FocusRequester() }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        // Email Field
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
            isError = viewModel.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(viewModel.email)
                .matches(),
            supportingText = {
                if (viewModel.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(viewModel.email)
                        .matches()
                ) {
                    Text("Please enter a valid email")
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { firstNameFocus.requestFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(emailFocus)
        )

        // Name Fields Row
        OutlinedTextField(
            value = viewModel.firstName,
            onValueChange = { viewModel.firstName = it },
            label = { Text("First name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { lastNameFocus.requestFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(firstNameFocus),
            supportingText = {
                // to keep the spaces consistent
            },
        )

        OutlinedTextField(
            value = viewModel.lastName,
            onValueChange = { viewModel.lastName = it },
            label = { Text("Last name") },
            leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = "Surname") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(lastNameFocus)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Date of Birth",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 4.dp)
        )

        DatePickerButton(
            selectedDate = viewModel.selectedDate,
            onDateSelected = { date ->
                viewModel.dateOfBirth = date
                viewModel.selectedDate = date
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Error message for date of birth

        Text(
            text =
                if (viewModel.selectedDate.isAfter(LocalDate.now().minusYears(18))) {
                    "You must be at least 18 years old"
                } else "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp)
        )


        // Password Fields
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = viewModel.password.isNotBlank() && viewModel.password.length < 8
                    || viewModel.password.isNotBlank() && !viewModel.password.matches(Regex(".*[A-Z].*"))
                    || viewModel.password.isNotBlank() && !viewModel.password.matches(Regex(".*[0-9].*"))
                    || viewModel.password.isNotBlank() && !viewModel.password.matches(Regex(".*[!@#$%^&*].*")),
            supportingText = {
                if (viewModel.password.isNotBlank() && viewModel.password.length < 8) {
                    Text("Password must be at least 8 characters")
                } else if (viewModel.password.isNotBlank() && !viewModel.password.matches(Regex(".*[A-Z].*"))) {
                    Text("Password must contain at least one uppercase letter")
                } else if (viewModel.password.isNotBlank() && !viewModel.password.matches(Regex(".*[0-9].*"))) {
                    Text("Password must contain at least one number")
                } else if (viewModel.password.isNotBlank() && !viewModel.password.matches(Regex(".*[!@#$%^&*].*"))) {
                    Text("Password must contain at least one special character")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { confirmPasswordFocus.requestFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocus)
        )

        OutlinedTextField(
            value = viewModel.confirmPassword,
            onValueChange = { viewModel.confirmPassword = it },
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(
                    Icons.Default.LockReset,
                    contentDescription = "Confirm Password"
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            isError = viewModel.confirmPassword.isNotBlank() && viewModel.password != viewModel.confirmPassword,
            supportingText = {
                if (viewModel.confirmPassword.isNotBlank() && viewModel.password != viewModel.confirmPassword) {
                    Text("Passwords don't match")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onDone = {
                onFinished()
            }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(confirmPasswordFocus)
        )
    }
}

@Composable
private fun ProfessionalInfoTab(viewModel: DoctorApplicationViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Speciality",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
            SpecialityDropdown(
                selectedSpeciality = viewModel.speciality,
                onSpecialitySelected = { viewModel.speciality = it }
            )
        }

        OutlinedTextField(
            value = viewModel.licenseNumber,
            onValueChange = { viewModel.licenseNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Medical License Number") },
            leadingIcon = {
                Icon(
                    Icons.Default.Badge,
                    contentDescription = "License Number"
                )
            },
            isError = viewModel.licenseNumber.isNotBlank() && !Regex("[A-Za-z]{2,3}\\d{4,8}").matches(
                viewModel.licenseNumber
            ),
            supportingText = {
                if (viewModel.licenseNumber.isNotBlank() && !Regex("[A-Za-z]{2,3}\\d{4,8}").matches(
                        viewModel.licenseNumber
                    )
                ) {
                    Text("Invalid license number format (should be 2-3 letters followed by 4-8 digits)")
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {


            FileUploadSection(
                title = "License Photo",
                fileUri = viewModel.licensePhotoUri,
                onFileSelected = { viewModel.licensePhotoUri = it },
                modifier = Modifier.weight(1f),
            )

            FileUploadSection(
                title = "Diploma",
                fileUri = viewModel.diplomaPhotoUri,
                onFileSelected = { viewModel.diplomaPhotoUri = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            )
        }

        OutlinedTextField(
            value = viewModel.yearsOfExperience,
            onValueChange = { viewModel.yearsOfExperience = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Years of Experience") },
            leadingIcon = {
                Icon(
                    Icons.Default.WorkHistory,
                    contentDescription = "Years of Experience"
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = (viewModel.yearsOfExperience.isNotBlank() && viewModel.yearsOfExperience.toIntOrNull() == null),
            supportingText = {
                if (viewModel.yearsOfExperience.isNotBlank() && viewModel.yearsOfExperience.toIntOrNull() == null) {
                    Text("Please enter a valid number")
                }
            }
        )

        OutlinedTextField(
            value = viewModel.currentWorkplace,
            onValueChange = { viewModel.currentWorkplace = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Current Workplace/Hospital") },
            leadingIcon = {
                Icon(
                    Icons.Default.LocalHospital,
                    contentDescription = "Workplace"
                )
            },
        )

        OutlinedTextField(
            value = viewModel.phoneNumber,
            onValueChange = { viewModel.phoneNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phone Number") },
            leadingIcon = {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = "Phone"
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = (viewModel.phoneNumber.isNotBlank() && !Regex("^\\+?[0-9]{8,15}\$").matches(
                viewModel.phoneNumber
            )),
            supportingText = {
                if (viewModel.phoneNumber.isNotBlank()) {
                    if (viewModel.phoneNumber.length < 8) {
                        Text("Phone number is too short")
                    } else if (viewModel.phoneNumber.length > 15) {
                        Text("Phone number is too long")
                    } else if (!Regex("^\\+?[0-9]{8,15}\$").matches(viewModel.phoneNumber)) {
                        Text("Please enter a valid phone number")
                    }
                }
            }
        )

        OutlinedTextField(
            value = viewModel.address,
            onValueChange = { viewModel.address = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Address") },
            leadingIcon = {
                Icon(
                    Icons.Default.PinDrop,
                    contentDescription = "Address"
                )
            },
            isError = viewModel.address.isBlank() && viewModel.hasSubmitted,
            supportingText = {
                if (viewModel.address.isBlank() && viewModel.hasSubmitted) {
                    Text("Address is required")
                }
            }
        )

        OutlinedTextField(
            value = viewModel.city,
            onValueChange = { viewModel.city = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("City") },
            leadingIcon = {
                Icon(
                    Icons.Default.LocationCity,
                    contentDescription = "City"
                )
            },
            isError = viewModel.city.isBlank() && viewModel.hasSubmitted,
            supportingText = {
                if (viewModel.city.isBlank() && viewModel.hasSubmitted) {
                    Text("City is required")
                }
            }
        )
    }
}


@Composable
fun SpecialityDropdown(
    selectedSpeciality: Speciality,
    onSpecialitySelected: (Speciality) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedSpeciality.displayName,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxSize(0.4f)
                .scrollable(rememberScrollState(), orientation = Orientation.Vertical)
        ) {
            Speciality.entries.forEach { speciality ->
                DropdownMenuItem(
                    text = {
                        Text(
                            speciality.displayName,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                    },
                    onClick = {
                        onSpecialitySelected(speciality)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FileUploadSection(
    title: String,
    fileUri: Uri?,
    onFileSelected: (Uri?) -> Unit,
    isError: Boolean = false,
    errorText: String = "",
    modifier: Modifier = Modifier
) {
    val launcher = rememberImagePickerLauncher(
        onImageSelected = { uri ->
            onFileSelected(uri)
        }
    )

    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )

        if (fileUri != null) {
            AsyncImage(
                model = fileUri,
                contentDescription = "Uploaded file",
                modifier = Modifier
                    .height(60.dp)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // Remove file button
            OutlinedButton(
                onClick = { onFileSelected(null) },
                modifier = Modifier
                    .padding(top = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Remove File")
            }
        } else {
            OutlinedButton(
                onClick = {
                    launcher.launch("image/*")
                },
                modifier = Modifier,
                border = BorderStroke(1.dp, BabyBlue),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BabyBlue,
                    containerColor = Color.Transparent
                )
            ) {
                Icon(Icons.Default.Upload, contentDescription = "Upload")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select File")
            }
        }

        if (isError) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

private fun isImageFile(uriString: String): Boolean {
    return uriString.contains(".jpg", ignoreCase = true) ||
            uriString.contains(".jpeg", ignoreCase = true) ||
            uriString.contains(".png", ignoreCase = true) ||
            uriString.contains(".webp", ignoreCase = true)
}

private fun isPdfFile(uriString: String): Boolean {
    return uriString.contains(".pdf", ignoreCase = true)
}

