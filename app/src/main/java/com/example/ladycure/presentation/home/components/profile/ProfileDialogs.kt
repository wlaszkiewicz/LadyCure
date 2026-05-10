package com.example.ladycure.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ladycure.R
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.presentation.register.components.DatePickerButton
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AccountSettingsDialog(
    userData: Map<String, Any>?,
    onDismiss: () -> Unit,
    onSave: (Map<String, String>) -> Unit,
    role: String? = null,
    onSaveDoctorProfile: ((Map<String, Any>) -> Unit)? = null
) {
    when (role) {
        "doctor" -> DoctorAccountSettingsDialog(
            userData = userData,
            onDismiss = onDismiss,
            onSave = { updatedData ->
                onSaveDoctorProfile?.invoke(updatedData as Map<String, Any>)
                onSave(updatedData.mapValues { it.value.toString() })
            }
        )

        else -> RegularAccountSettingsDialog(userData, onDismiss, onSave)
    }
}

@Composable
fun DoctorAccountSettingsDialog(
    userData: Map<String, Any>?,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit,
    role: String? = null
) {
    val dimens = rememberResponsiveDimens()
    var name by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("name") as? String) ?: ""
            )
        )
    }
    var nameError by remember { mutableStateOf("") }
    var surname by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("surname") as? String) ?: ""
            )
        )
    }
    var surnameError by remember { mutableStateOf("") }

    val initialDob = remember {
        try {
            (userData?.get("dob") as? String)?.let {
                LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
            } ?: LocalDate.now().minusYears(18)
        } catch (e: Exception) {
            LocalDate.now().minusYears(18)
        }
    }
    var dob by remember { mutableStateOf(initialDob) }
    var dobText by remember { mutableStateOf(initialDob.format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var isAdult by remember { mutableStateOf(!dob.isAfter(LocalDate.now().minusYears(18))) }
    var dobError by remember { mutableStateOf("") }

    var selectedSpeciality by remember {
        mutableStateOf(
            Speciality.fromDisplayName(userData?.get("speciality") as? String ?: "")
                ?: Speciality.OTHER
        )
    }
    var expanded by remember { mutableStateOf(false) }

    var phone by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("phone") as? String) ?: ""
            )
        )
    }
    var phoneError by remember { mutableStateOf("") }
    var address by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("address") as? String) ?: ""
            )
        )
    }
    var addressError by remember { mutableStateOf("") }
    var city by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("city") as? String) ?: ""
            )
        )
    }
    var cityError by remember { mutableStateOf("") }

    var consultationPrice by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("consultationPrice") as? Number)?.toString() ?: ""
            )
        )
    }
    var consultationPriceError by remember { mutableStateOf("") }
    var experience by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("experience") as? Number)?.toString() ?: ""
            )
        )
    }
    var experienceError by remember { mutableStateOf("") }
    var languages by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("languages") as? List<String>)?.joinToString(
                    ", "
                ) ?: ""
            )
        )
    }
    var languagesError by remember { mutableStateOf("") }
    var specialityError by remember { mutableStateOf("") }

    var bio by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("bio") as? String) ?: ""
            )
        )
    }
    var bioError by remember { mutableStateOf("") }

    val validateInputs: () -> Boolean = {
        var isValid = true

        if (name.text.isBlank()) {
            nameError = "Name cannot be empty"
            isValid = false
        } else if (name.text.length > 50) {
            nameError = "Name is too long (max 50 characters)"
            isValid = false
        } else {
            nameError = ""
        }

        if (surname.text.isBlank()) {
            surnameError = "Surname cannot be empty"
            isValid = false
        } else if (surname.text.length > 50) {
            surnameError = "Surname is too long (max 50 characters)"
            isValid = false
        } else {
            surnameError = ""
        }

        if (dobText.isBlank()) {
            dobError = "Date of birth cannot be empty"
            isValid = false
        } else if (!isValidBirthDate(dobText)) {
            dobError = "Date of birth must be in OSCE-MM-dd format"
            isValid = false
        } else if (!isAdult) {
            dobError = "You must be at least 18 years old"
            isValid = false
        } else {
            dobError = ""
        }


        if (phone.text.isBlank()) {
            phoneError = "Phone number cannot be empty"
            isValid = false
        } else if (!isValidPhone(phone.text)) {
            phoneError = "Please enter a valid phone number"
            isValid = false
        } else {
            phoneError = ""
        }

        val price = consultationPrice.text.toDoubleOrNull()
        if (consultationPrice.text.isBlank()) {
            consultationPriceError = "Consultation price cannot be empty"
            isValid = false
        } else if (price == null || price <= 0) {
            consultationPriceError = "Consultation price must be a positive number"
            isValid = false
        } else {
            consultationPriceError = ""
        }

        val exp = experience.text.toIntOrNull()
        if (experience.text.isBlank()) {
            experienceError = "Experience cannot be empty"
            isValid = false
        } else if (exp == null || exp < 0) {
            experienceError = "Experience must be a non-negative number"
            isValid = false
        } else {
            experienceError = ""
        }

        if (languages.text.isBlank()) {
            languagesError = "At least one language must be specified"
            isValid = false
        } else {
            languagesError = ""
        }


        if (address.text.isBlank()) {
            addressError = "Address cannot be empty"
            isValid = false
        } else {
            addressError = ""
        }

        if (city.text.isBlank()) {
            cityError = "City cannot be empty"
            isValid = false
        } else {
            cityError = ""
        }

        if (bio.text.isBlank()) {
            bioError = "Bio cannot be empty"
            isValid = false
        } else if (bio.text.length < 20) {
            bioError = "Bio should be at least 20 characters"
            isValid = false
        } else {
            bioError = ""
        }

        isValid
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(DefaultBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.w(16 / 411f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(dimens.w(32 / 411f))
                        .clickable { onDismiss() },
                    tint = DefaultPrimary
                )

                Text(
                    text = "Doctor Account Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = DefaultPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(dimens.w(32 / 411f)))
            }

            Box(
                modifier = Modifier
                    .size(dimens.w(160 / 411f))
                    .clip(CircleShape)
                    .background(DefaultPrimary.copy(alpha = 0.1f))
                    .border(2.dp, DefaultPrimary.copy(alpha = 0.3f), CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.setting_kapii),
                    contentDescription = "settings kapi",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

            Column(
                modifier = Modifier
                    .padding(horizontal = dimens.w(16 / 411f), vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimens.h(16 / 914f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(dimens.w(16 / 411f))
                    ) {
                        Text(
                            text = "Basic Information",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = DefaultPrimary,
                            modifier = Modifier.padding(bottom = dimens.h(12 / 914f))
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; nameError = "" },
                            label = { Text("Name") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Name"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            ),
                            isError = nameError.isNotEmpty(),
                            supportingText = { if (nameError.isNotEmpty()) Text(nameError) }
                        )

                        Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

                        OutlinedTextField(
                            value = surname,
                            onValueChange = { surname = it; surnameError = "" },
                            label = { Text("Surname") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = "Surname"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = DefaultPrimary,
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = DefaultPrimary
                            ),
                            isError = surnameError.isNotEmpty(),
                            supportingText = { if (surnameError.isNotEmpty()) Text(surnameError) }
                        )

                        Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; phoneError = "" },
                            label = { Text("Phone Number") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = "Phone"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("+48 123 456 789") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            ),
                            isError = phoneError.isNotEmpty(),
                            supportingText = { if (phoneError.isNotEmpty()) Text(phoneError) }
                        )

                        Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Date of Birth",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            DatePickerButton(
                                selectedDate = dob,
                                onDateSelected = { date ->
                                    dob = date
                                    dobText = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                    isAdult = !date.isAfter(LocalDate.now().minusYears(18))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (dobError.isNotEmpty()) {
                                Text(
                                    text = dobError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimens.h(16 / 914f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(dimens.w(16 / 411f))
                    ) {
                        Text(
                            text = "Professional Information",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = DefaultPrimary,
                            modifier = Modifier.padding(bottom = dimens.h(12 / 914f))
                        )

                        OutlinedTextField(
                            value = experience,
                            onValueChange = { experience = it; experienceError = "" },
                            label = { Text("Experience (years)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Experience"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            ),
                            isError = experienceError.isNotEmpty(),
                            supportingText = {
                                if (experienceError.isNotEmpty()) Text(
                                    experienceError
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

                        OutlinedTextField(
                            value = consultationPrice,
                            onValueChange = { consultationPrice = it; consultationPriceError = "" },
                            label = { Text("Consultation Price") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    contentDescription = "Price"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            prefix = { Text("$") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            ),
                            isError = consultationPriceError.isNotEmpty(),
                            supportingText = {
                                if (consultationPriceError.isNotEmpty()) Text(
                                    consultationPriceError
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

                        OutlinedTextField(
                            value = languages,
                            onValueChange = { languages = it; languagesError = "" },
                            label = { Text("Languages (comma separated)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = "Languages"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            ),
                            isError = languagesError.isNotEmpty(),
                            supportingText = { if (languagesError.isNotEmpty()) Text(languagesError) }
                        )

                        Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it; bioError = "" },
                            label = { Text("Bio") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Bio"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            ),
                            isError = bioError.isNotEmpty(),
                            supportingText = { if (bioError.isNotEmpty()) Text(bioError) }
                        )
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimens.h(16 / 914f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(dimens.w(16 / 411f))
                    ) {
                        Text(
                            text = "Speciality",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = DefaultPrimary,
                            modifier = Modifier.padding(bottom = dimens.h(12 / 914f))
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = true }
                                    .border(
                                        1.dp,
                                        if (specialityError.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.2f
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(dimens.w(16 / 411f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedSpeciality.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand",
                                        modifier = Modifier.rotate(if (expanded) 180f else 0f)
                                    )
                                }
                            }
                            if (specialityError.isNotEmpty()) {
                                Text(
                                    text = specialityError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(
                                        start = dimens.w(16 / 411f),
                                        top = 4.dp
                                    )
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                Speciality.values().forEach { specialityItem ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = specialityItem.displayName,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        onClick = {
                                            selectedSpeciality = specialityItem
                                            specialityError = ""
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimens.h(16 / 914f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(dimens.w(16 / 411f))
                    ) {
                        Text(
                            text = "Address Information",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = DefaultPrimary,
                            modifier = Modifier.padding(bottom = dimens.h(12 / 914f))
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it; addressError = "" },
                            label = { Text("Address") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = "Address"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            ),
                            isError = addressError.isNotEmpty(),
                            supportingText = { if (addressError.isNotEmpty()) Text(addressError) }
                        )

                        Spacer(modifier = Modifier.height(dimens.h(12 / 914f)))

                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it; cityError = "" },
                            label = { Text("City") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = "City") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DefaultPrimary,
                                focusedLabelColor = DefaultPrimary
                            ),
                            isError = cityError.isNotEmpty(),
                            supportingText = { if (cityError.isNotEmpty()) Text(cityError) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimens.h(16 / 914f)),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(end = dimens.w(16 / 411f))
                            .width(dimens.w(120 / 411f)),
                        border = BorderStroke(1.dp, DefaultPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DefaultPrimary)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (validateInputs()) {
                                val updatedData = mapOf(
                                    "name" to name.text,
                                    "surname" to surname.text,
                                    "dob" to dobText,
                                    "phone" to phone.text,
                                    "address" to address.text,
                                    "city" to city.text,
                                    "consultationPrice" to (consultationPrice.text.toDoubleOrNull()
                                        ?: 0.0),
                                    "experience" to (experience.text.toIntOrNull()
                                        ?: 0),
                                    "languages" to languages.text.split(",").map { it.trim() },
                                    "speciality" to selectedSpeciality.displayName,
                                    "bio" to bio.text
                                )
                                onSave(updatedData)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary,
                            contentColor = Color.White
                        ),
                        enabled = validateInputs(),
                        modifier = Modifier.width(dimens.w(140 / 411f)),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun RegularAccountSettingsDialog(
    userData: Map<String, Any>?,
    onDismiss: () -> Unit,
    onSave: (Map<String, String>) -> Unit
) {
    val dimens = rememberResponsiveDimens()
    var name by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("name") as? String) ?: ""
            )
        )
    }
    var nameError by remember { mutableStateOf("") }
    var surname by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("surname") as? String) ?: ""
            )
        )
    }
    var surnameError by remember { mutableStateOf("") }

    val initialDob = remember {
        try {
            (userData?.get("dob") as? String)?.let {
                LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
            } ?: LocalDate.now().minusYears(18)
        } catch (e: Exception) {
            LocalDate.now().minusYears(18)
        }
    }
    var dob by remember { mutableStateOf(initialDob) }
    var dobText by remember { mutableStateOf(initialDob.format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var isAdult by remember { mutableStateOf(!dob.isAfter(LocalDate.now().minusYears(18))) }
    var dobError by remember { mutableStateOf("") }

    var phone by remember {
        mutableStateOf(
            TextFieldValue(
                (userData?.get("phone") as? String) ?: ""
            )
        )
    }
    var phoneError by remember { mutableStateOf("") }

    val validateInputs: () -> Boolean = {
        var isValid = true

        if (name.text.isBlank()) {
            nameError = "Name cannot be empty"
            isValid = false
        } else if (name.text.length > 50) {
            nameError = "Name is too long (max 50 characters)"
            isValid = false
        } else {
            nameError = ""
        }

        if (surname.text.isBlank()) {
            surnameError = "Surname cannot be empty"
            isValid = false
        } else if (surname.text.length > 50) {
            surnameError = "Surname is too long (max 50 characters)"
            isValid = false
        } else {
            surnameError = ""
        }

        if (dobText.isBlank()) {
            dobError = "Date of birth cannot be empty"
            isValid = false
        } else if (!isValidBirthDate(dobText)) {
            dobError = "Date of birth must be in OSCE-MM-dd format"
            isValid = false
        } else if (!isAdult) {
            dobError = "We are sorry, you must be at least 18 years old"
            isValid = false
        } else {
            dobError = ""
        }

        if (phone.text.isBlank()) {
//            phoneError = "Phone number cannot be empty"
//            isValid = false
        } else if (!isValidPhone(phone.text)) {
            phoneError = "Please enter a valid phone number"
            isValid = false
        } else {
            phoneError = ""
        }

        isValid
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onDismiss)
        )
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(
                    color = DefaultBackground,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.width(dimens.w(320 / 411f)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = DefaultOnPrimary
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = dimens.w(20 / 411f), vertical = dimens.h(24 / 914f))
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimens.w(150 / 411f))
                            .clip(CircleShape)
                            .background(DefaultPrimary.copy(alpha = 0.1f))
                            .border(2.dp, DefaultPrimary.copy(alpha = 0.3f), CircleShape)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.setting_kapii),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

                    Text(
                        text = "Account Settings",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = DefaultPrimary,
                        modifier = Modifier.padding(bottom = dimens.h(16 / 914f))
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameError = "" },
                        label = { Text("Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError.isNotEmpty(),
                        supportingText = { if (nameError.isNotEmpty()) Text(nameError) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = surname,
                        onValueChange = { surname = it; surnameError = "" },
                        label = { Text("Surname") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Surname"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = surnameError.isNotEmpty(),
                        supportingText = { if (surnameError.isNotEmpty()) Text(surnameError) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; phoneError = "" },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("+48 123 456 789") },
                        isError = phoneError.isNotEmpty(),
                        supportingText = { if (phoneError.isNotEmpty()) Text(phoneError) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Date of Birth",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        DatePickerButton(
                            selectedDate = dob,
                            onDateSelected = { date ->
                                dob = date
                                dobText = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                isAdult = !date.isAfter(LocalDate.now().minusYears(18))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (dobError.isNotEmpty()) {
                            Text(
                                text = dobError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = dimens.w(16 / 411f), top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DefaultOnPrimary.copy(alpha = 0.1f),
                                contentColor = DefaultPrimary
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (validateInputs()) {
                                    val updatedData = mapOf(
                                        "name" to name.text,
                                        "surname" to surname.text,
                                        "dob" to dobText,
                                        "phone" to phone.text
                                    )
                                    onSave(updatedData)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DefaultPrimary,
                                contentColor = DefaultOnPrimary
                            ),
                            enabled = validateInputs()
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

private fun isValidBirthDate(date: String): Boolean {
    val pattern = Regex("""^\d{4}-\d{2}-\d{2}$""")
    if (!pattern.matches(date)) return false

    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.isLenient = false
        dateFormat.parse(date)
        true
    } catch (e: Exception) {
        false
    }
}

private fun isValidPhone(phone: String): Boolean {
    return phone.matches(Regex("""^[+]?[\d\s-]{6,15}$"""))
}
